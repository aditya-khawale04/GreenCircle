package com.example.greencircle;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GardenMentorBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "GardenMentor";

    // UI Components
    private LinearLayout chatContainer;
    private ScrollView chatScrollView;
    private EditText etQuery;

    // Gemini Model
    private GenerativeModelFutures model;
    private Executor executor;

    // System prompt for context
    private static final String SYSTEM_INSTRUCTION =
            "You are a helpful and knowledgeable gardening assistant. " +
                    "Provide practical, concise advice about plants, gardening techniques, " +
                    "pest control, soil care, and plant maintenance. " +
                    "Keep responses brief (2-3 sentences) unless asked for detailed information. " +
                    "Be friendly and encouraging.";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_garden_mentor, container, false);

        // Initialize Views
        chatContainer = view.findViewById(R.id.chatContainer);
        chatScrollView = view.findViewById(R.id.chatScrollView);
        etQuery = view.findViewById(R.id.etQuery);
        ImageView btnClose = view.findViewById(R.id.btnClose);
        FloatingActionButton btnSend = view.findViewById(R.id.btnSend);

        // Initialize Gemini
        initializeGemini();

        // Initialize Executor
        executor = Executors.newSingleThreadExecutor();

        // Close Button
        btnClose.setOnClickListener(v -> dismiss());

        // Send Button
        btnSend.setOnClickListener(v -> {
            String query = etQuery.getText().toString().trim();
            if (!query.isEmpty()) {
                sendMessage(query);
            }
        });

        // Add welcome message
        addAiBubble("Hello! I'm your Garden Mentor. Ask me anything about gardening, plants, or growing tips! 🌱");

        return view;
    }

    private void initializeGemini() {
        String apiKey = BuildConfig.GEMINI_API_KEY;

        Log.d(TAG, "Initializing Gemini with API key: " + (apiKey != null ? "Present" : "Missing"));

        String modelName = "gemini-3-flash-preview";
        Log.d(TAG, "Using model: " + modelName);

        GenerativeModel gm = new GenerativeModel(modelName, apiKey);
        model = GenerativeModelFutures.from(gm);
    }

    private void sendMessage(String userMessage) {
        // Null check for context
        if (getContext() == null) return;

        // Add User Message to UI
        addUserBubble(userMessage);
        etQuery.setText("");

        // Add a temporary "Thinking..." bubble
        TextView thinkingBubble = addAiBubble("Thinking...");

        // Create prompt with system instruction and user message
        String fullPrompt = SYSTEM_INSTRUCTION + "\n\nUser question: " + userMessage;

        // Create content
        Content.Builder contentBuilder = new Content.Builder();
        contentBuilder.addText(fullPrompt);
        Content content = contentBuilder.build();

        // Generate content
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        // Handle Response asynchronously
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String aiText = result.getText();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Remove "Thinking..." and show real answer
                        chatContainer.removeView(thinkingBubble);
                        addAiBubble(aiText);
                    });
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Gemini API Error", t);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        chatContainer.removeView(thinkingBubble);
                        addAiBubble("Sorry, I had trouble connecting to the garden server. Please check your internet connection and try again. 🌿");
                    });
                }
            }
        }, executor);
    }

    // Helper to create User Bubble (Right Aligned, Green)
    private void addUserBubble(String text) {
        if (getContext() == null) return;

        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextSize(16);
        textView.setTextColor(Color.WHITE);
        textView.setPadding(32, 24, 32, 24);
        textView.setBackgroundResource(R.drawable.bg_input_field);
        textView.getBackground().setTint(Color.parseColor("#0F8E5F")); // Green background

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.END; // Align Right
        params.setMargins(100, 16, 0, 16); // Left margin to push it right
        textView.setLayoutParams(params);
        textView.setMaxWidth(getResources().getDisplayMetrics().widthPixels - 150);

        chatContainer.addView(textView);
        scrollToBottom();
    }

    // Helper to create AI Bubble (Left Aligned, Light Blue/Grey)
    private TextView addAiBubble(String text) {
        if (getContext() == null) return null;

        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextSize(16);
        textView.setTextColor(Color.parseColor("#1F2937"));
        textView.setPadding(32, 24, 32, 24);
        textView.setBackgroundResource(R.drawable.bg_input_field);
        textView.getBackground().setTint(Color.parseColor("#EFF6FF")); // Light Grey/Blue

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.START; // Align Left
        params.setMargins(0, 16, 100, 16); // Right margin to keep it left
        textView.setLayoutParams(params);
        textView.setMaxWidth(getResources().getDisplayMetrics().widthPixels - 150);

        chatContainer.addView(textView);
        scrollToBottom();
        return textView;
    }

    private void scrollToBottom() {
        if (chatScrollView != null) {
            chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Shutdown executor to prevent memory leaks
        if (executor instanceof ExecutorService) {
            ((ExecutorService) executor).shutdown();
        }
    }
}