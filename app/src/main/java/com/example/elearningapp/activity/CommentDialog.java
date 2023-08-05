package com.example.elearningapp.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elearningapp.R;
import com.example.elearningapp.adapter.CommentAdapter;
import com.example.elearningapp.object.CommentObject;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentDialog extends Dialog {

    private List<CommentObject> commentObjects;

    CommentAdapter commentAdapter;

    String courseId;
    String lessonId;
    String userId;

    EditText replyCommentField;

    ImageButton replySendButton;

    RadioGroup filterComment;
    RadioButton newFilterComment;
    RadioButton popularFilterComment;
    ImageView replyuserpic;

    ProgressBar progressBar;
    RecyclerView listComment;

    View thisView;

    public CommentDialog(Context context,
                         String courseId,
                         String lessonId,
                         String userId) {
        super(context);
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.userId = userId;
    }

    public EditText getCommentEditText() {
        return replyCommentField;
    }

    private void setNewComment() {
        setUpRecyclerView(thisView);
        listComment.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("comments")
                .orderBy("timestamp")
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("lessonId", lessonId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        for (DocumentChange doc: value.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                QueryDocumentSnapshot documentSnapshot = doc.getDocument();
                                List <String> likeList = (List<String>) documentSnapshot.get("like");
                                commentObjects.add(new CommentObject(
                                        documentSnapshot.getString("content"),
                                        documentSnapshot.getId(),
                                        1,
                                        documentSnapshot.getString("userId"),
                                        likeList,
                                        documentSnapshot.getLong("timestamp")
                                ));
                                commentAdapter.notifyDataSetChanged();
                            }
                        }

                        Handler handler = new Handler();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                listComment.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        };
                        handler.postDelayed(runnable, 500);
                    }
                });
    }

    public void setPopularComment() {
        setUpRecyclerView(thisView);
        listComment.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore.getInstance().collection("comments")
                .orderBy("likeCnt")
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("lessonId", lessonId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        for (DocumentChange doc: value.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                QueryDocumentSnapshot documentSnapshot = doc.getDocument();
                                List <String> likeList = (List<String>) documentSnapshot.get("like");
                                commentObjects.add(new CommentObject(
                                        documentSnapshot.getString("content"),
                                        documentSnapshot.getId(),
                                        1,
                                        documentSnapshot.getString("userId"),
                                        likeList,
                                        documentSnapshot.getLong("timestamp")
                                ));
                                commentAdapter.notifyDataSetChanged();
                            }
                        }

                        Handler handler = new Handler();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                listComment.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        };
                        handler.postDelayed(runnable, 500);
                    }
                });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_comment, null);
        setContentView(view);

        this.thisView = view;


        replySendButton = view.findViewById(R.id.replySendButton);
        replyCommentField = view.findViewById(R.id.replyCommentField);
        filterComment = view.findViewById(R.id.filterComment);
        newFilterComment = view.findViewById(R.id.newFilterComment);
        popularFilterComment = view.findViewById(R.id.popularFilterComment);
        replyuserpic = view.findViewById(R.id.replyuserpic);
        progressBar = view.findViewById(R.id.loadingComment);
        listComment = view.findViewById(R.id.listComment);

        newFilterComment.setChecked(true);
        setNewComment();


        FirebaseFirestore.getInstance().collection("users").document(userId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        Picasso.get().load(value.getString("image")).into(replyuserpic);
                    }
                });


        newFilterComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNewComment();
            }
        });

        popularFilterComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPopularComment();
            }
        });




        replySendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (replyCommentField.getText().length() < 5) {
                    return;
                }
                if (commentAdapter.getUserReplyIdGet().equals("")) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("content", replyCommentField.getText().toString());
                    data.put("courseId", courseId);
                    data.put("like", Arrays.asList("xyz"));
                    data.put("lessonId", lessonId);
                    data.put("timestamp", System.currentTimeMillis());
                    data.put("userId", userId);
                    data.put("likeCnt", 0);
                    FirebaseFirestore.getInstance().collection("comments")
                            .add(data).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    replyCommentField.setText("");
                                    showToast();
                                }
                            });


                } else {
                    Map<String, Object> data = new HashMap<>();
                    data.put("content", replyCommentField.getText().toString());
                    data.put("like", Arrays.asList("xyz"));
                    data.put("timestamp", System.currentTimeMillis());
                    data.put("userId", userId);
                    data.put("userReplyId", commentAdapter.getUserReplyIdGet());
                    data.put("likeCnt", 0);
                    FirebaseFirestore.getInstance().collection("comments")
                            .document(commentAdapter.getCommentIdReply())
                            .collection("replies")
                            .add(data).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    replyCommentField.setText("");
                                    showToast();
                                }
                            });

                }

            }
        });

    }

    private void showToast() {
        Log.v("Comment", "ook");
        LayoutInflater layoutInflater = getLayoutInflater();
        View layout = layoutInflater.inflate(R.layout.toast_layout, null);
        Toast toast = new Toast(getContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);

        toast.show();
    }

    public RecyclerView getRecyclerView() {
        if (thisView != null) {
            return thisView.findViewById(R.id.listComment);
        }
        return null;
    }

    private void setUpRecyclerView(View view) {
        commentObjects = new ArrayList<>();
        commentAdapter = new CommentAdapter(getContext(), commentObjects);

        commentAdapter.setDialog(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        listComment.setLayoutManager(layoutManager);
//        commentAdapter = new CommentAdapter(getContext(), commentObjects);

        listComment.setAdapter(commentAdapter);
    }
}