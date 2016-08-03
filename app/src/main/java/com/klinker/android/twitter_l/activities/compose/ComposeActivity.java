package com.klinker.android.twitter_l.activities.compose;
/*
 * Copyright 2014 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.bumptech.glide.Glide;
import com.klinker.android.twitter_l.R;
import com.klinker.android.twitter_l.adapters.AutoCompleteHashtagAdapter;
import com.klinker.android.twitter_l.adapters.AutoCompletePeopleAdapter;
import com.klinker.android.twitter_l.data.sq_lite.FollowersDataSource;
import com.klinker.android.twitter_l.data.sq_lite.HashtagDataSource;
import com.klinker.android.twitter_l.data.sq_lite.QueuedDataSource;
import com.klinker.android.twitter_l.views.widgets.FontPrefEditText;
import com.klinker.android.twitter_l.views.widgets.FontPrefTextView;
import com.klinker.android.twitter_l.activities.GiphySearch;
import com.klinker.android.twitter_l.activities.scheduled_tweets.ViewScheduledTweets;
import com.klinker.android.twitter_l.utils.IOUtils;
import com.klinker.android.twitter_l.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ComposeActivity extends Compose {

    @Override
    public void onDestroy() {
        try {
            ((AutoCompletePeopleAdapter) userAutoComplete.getListView().getAdapter()).getCursor().close();
        } catch (Exception e) {

        }
        super.onDestroy();
    }

    public void setUpLayout() {
        setContentView(R.layout.compose_activity);

        setUpSimilar();

        cancelButton[0] = (ImageButton) findViewById(R.id.cancel1);
        cancelButton[1] = (ImageButton) findViewById(R.id.cancel2);
        cancelButton[2] = (ImageButton) findViewById(R.id.cancel3);
        cancelButton[3] = (ImageButton) findViewById(R.id.cancel4);
        holders[0] = (FrameLayout) findViewById(R.id.holder1);
        holders[1] = (FrameLayout) findViewById(R.id.holder2);
        holders[2] = (FrameLayout) findViewById(R.id.holder3);
        holders[3] = (FrameLayout) findViewById(R.id.holder4);

        for (int i = 0; i < cancelButton.length; i++) {
            final int pos = i;
            cancelButton[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imagesAttached--;

                    List<String> uris = new ArrayList<String>();
                    for (String uri : attachedUri) {
                        uris.add(uri);

                    }
                    uris.remove(pos);

                    for (int i = 0; i < attachImage.length; i++) {
                        attachImage[i].setImageDrawable(null);
                        attachedUri[i] = null;
                        holders[i].setVisibility(View.GONE);
                    }
                    for (int i = 0; i < imagesAttached; i++) {
                        attachImage[i].setImageURI(Uri.parse(uris.get(i)));
                        attachedUri[i] = uris.get(i);
                        holders[i].setVisibility(View.VISIBLE);
                    }

                    attachButton.setEnabled(true);
                }
            });
        }

        int count = 0; // number of accounts logged in

        if (sharedPrefs.getBoolean("is_logged_in_1", false)) {
            count++;
        }

        if (sharedPrefs.getBoolean("is_logged_in_2", false)) {
            count++;
        }

        if (count == 2) {
            findViewById(R.id.accounts).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] options = new String[3];

                    options[0] = "@" + settings.myScreenName;
                    options[1] = "@" + settings.secondScreenName;
                    options[2] = getString(R.string.both_accounts);

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int item) {
                            ImageView pic = (ImageView) findViewById(R.id.profile_pic);
                            FontPrefTextView currentName = (FontPrefTextView) findViewById(R.id.current_name);

                            switch (item) {
                                case 0:
                                    useAccOne = true;
                                    useAccTwo = false;

                                    Glide.with(ComposeActivity.this).load(settings.myProfilePicUrl).into(pic);
                                    currentName.setText("@" + settings.myScreenName);

                                    String tweetText = reply.getText().toString();
                                    tweetText = tweetText.replace("@" + settings.myScreenName + " ", "")
                                                    .replace("@" + settings.myScreenName, "");

                                    reply.setText(tweetText);
                                    reply.setSelection(tweetText.length());

                                    break;
                                case 1:
                                    useAccOne = false;
                                    useAccTwo = true;

                                    Glide.with(ComposeActivity.this).load(settings.secondProfilePicUrl).into(pic);
                                    currentName.setText("@" + settings.secondScreenName);

                                    tweetText = reply.getText().toString();
                                    tweetText = tweetText.replace("@" + settings.secondScreenName + " ", "")
                                                    .replace("@" + settings.secondScreenName, "");

                                    reply.setText(tweetText);
                                    reply.setSelection(tweetText.length());

                                    break;
                                case 2:
                                    useAccOne = true;
                                    useAccTwo = true;

                                    TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.bothAccounts});
                                    int resource = a.getResourceId(0, 0);
                                    a.recycle();
                                    pic.setImageResource(resource);

                                    currentName.setText(getString(R.string.both_accounts));

                                    break;
                            }
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        userAutoComplete = new ListPopupWindow(context);
        userAutoComplete.setAnchorView(reply);
        userAutoComplete.setHeight(toDP(200));
        userAutoComplete.setWidth((int)(width * .75));
        userAutoComplete.setAdapter(new AutoCompletePeopleAdapter(context,
                FollowersDataSource.getInstance(context).getCursor(currentAccount, reply.getText().toString()), reply));
        userAutoComplete.setPromptPosition(ListPopupWindow.POSITION_PROMPT_BELOW);

        userAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                userAutoComplete.dismiss();
            }
        });

        hashtagAutoComplete = new ListPopupWindow(context);
        hashtagAutoComplete.setAnchorView(reply);
        hashtagAutoComplete.setHeight(toDP(200));
        hashtagAutoComplete.setWidth((int)(width * .75));
        hashtagAutoComplete.setAdapter(new AutoCompleteHashtagAdapter(context,
                HashtagDataSource.getInstance(context).getCursor(reply.getText().toString()), reply));
        hashtagAutoComplete.setPromptPosition(ListPopupWindow.POSITION_PROMPT_ABOVE);

        hashtagAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                hashtagAutoComplete.dismiss();
            }
        });

        // watcher for the @
        reply.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                String searchText = reply.getText().toString();

                try {
                    int position = reply.getSelectionStart() - 1;
                    if (searchText.charAt(position) == '@') {
                        userAutoComplete.show();
                    } else if (searchText.charAt(position) == ' ') {
                        userAutoComplete.dismiss();
                    } else if (userAutoComplete.isShowing()) {
                        String adapterText = "";
                        do {
                            adapterText = searchText.charAt(position--) + adapterText;
                        } while (searchText.charAt(position) != '@');
                        adapterText = adapterText.replace("@", "");
                        userAutoComplete.setAdapter(new AutoCompletePeopleAdapter(context,
                                FollowersDataSource.getInstance(context).getCursor(currentAccount, adapterText), reply));
                    }

                    position = reply.getSelectionStart() - 1;
                    if (searchText.charAt(position) == '#') {
                        hashtagAutoComplete.show();
                    } else if (searchText.charAt(position) == ' ') {
                        hashtagAutoComplete.dismiss();
                    } else if (hashtagAutoComplete.isShowing()) {
                        String adapterText = "";
                        do {
                            adapterText = searchText.charAt(position--) + adapterText;
                        } while (searchText.charAt(position) != '#');
                        adapterText = adapterText.replace("#", "");
                        hashtagAutoComplete.setAdapter(new AutoCompleteHashtagAdapter(context,
                                HashtagDataSource.getInstance(context).getCursor(adapterText), reply));
                    }
                } catch (Exception e) {
                    // there is no text
                    try {
                        userAutoComplete.dismiss();
                    } catch (Exception x) {
                        // something went really wrong I guess haha
                    }

                    try {
                        hashtagAutoComplete.dismiss();
                    } catch (Exception x) {
                        // something went really wrong I guess haha
                    }
                }

            }
        });

        overflow = (ImageButton) findViewById(R.id.overflow_button);
        overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attachImage();
            }
        });

        gifButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findGif();
            }
        });
        ImageButton at = (ImageButton) findViewById(R.id.at_button);
        at.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int start = reply.getSelectionStart();
                reply.getText().insert(start, "@");
                reply.setSelection(start + 1);
            }
        });

        ImageButton hashtag = (ImageButton) findViewById(R.id.hashtag_button);
        hashtag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int start = reply.getSelectionStart();
                reply.getText().insert(start, "#");
                reply.setSelection(start + 1);
            }
        });

        final int SAVE_DRAFT = 0;
        final int VIEW_DRAFTS = 1;
        final int VIEW_QUEUE = 2;
        final int SCHEDULE = 3;

        final ImageButton overflow = (ImageButton) findViewById(R.id.overflow_button);
        overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu menu = new PopupMenu(context, findViewById(R.id.overflow_button));

                menu.getMenu().add(Menu.NONE, SAVE_DRAFT, Menu.NONE, context.getString(R.string.menu_save_draft));
                menu.getMenu().add(Menu.NONE, VIEW_DRAFTS, Menu.NONE, context.getString(R.string.menu_view_drafts));
                menu.getMenu().add(Menu.NONE, VIEW_QUEUE, Menu.NONE, context.getString(R.string.menu_view_queued));
                menu.getMenu().add(Menu.NONE, SCHEDULE, Menu.NONE, context.getString(R.string.menu_schedule_tweet));

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case SAVE_DRAFT:
                                if (reply.getText().length() > 0) {
                                    QueuedDataSource.getInstance(context).createDraft(reply.getText().toString(), currentAccount);
                                    Toast.makeText(context, getResources().getString(R.string.saved_draft), Toast.LENGTH_SHORT).show();
                                    reply.setText("");
                                    finish();
                                } else {
                                    Toast.makeText(context, getResources().getString(R.string.no_tweet), Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case VIEW_DRAFTS:
                                final String[] drafts = QueuedDataSource.getInstance(context).getDrafts();
                                if (drafts.length > 0) {
                                    final String[] draftsAndDelete = new String[drafts.length + 1];
                                    draftsAndDelete[0] = getString(R.string.delete_all);
                                    for (int i = 1; i < draftsAndDelete.length; i++) {
                                        draftsAndDelete[i] = drafts[i - 1];
                                    }

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setItems(draftsAndDelete, new DialogInterface.OnClickListener() {
                                        public void onClick(final DialogInterface dialog, final int item) {

                                            if (item == 0) {
                                                // clicked the delete all item
                                                new AlertDialog.Builder(context)
                                                        .setMessage(getString(R.string.delete_all) + "?")
                                                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                QueuedDataSource.getInstance(context).deleteAllDrafts();
                                                                dialogInterface.dismiss();
                                                            }
                                                        })
                                                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                dialogInterface.dismiss();
                                                            }
                                                        })
                                                        .create()
                                                        .show();

                                                dialog.dismiss();
                                            } else {
                                                new AlertDialog.Builder(context)
                                                        .setTitle(context.getResources().getString(R.string.apply))
                                                        .setMessage(draftsAndDelete[item])
                                                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                reply.setText(draftsAndDelete[item]);
                                                                reply.setSelection(reply.getText().length());
                                                                QueuedDataSource.getInstance(context).deleteDraft(draftsAndDelete[item]);
                                                                dialogInterface.dismiss();
                                                            }
                                                        })
                                                        .setNegativeButton(R.string.delete_draft, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                QueuedDataSource.getInstance(context).deleteDraft(draftsAndDelete[item]);
                                                                dialogInterface.dismiss();
                                                            }
                                                        })
                                                        .create()
                                                        .show();

                                                dialog.dismiss();
                                            }
                                        }
                                    });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                } else {
                                    Toast.makeText(context, R.string.no_drafts, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case SCHEDULE:
                                Intent schedule = new Intent(context, ViewScheduledTweets.class);
                                if (!reply.getText().toString().isEmpty()) {
                                    schedule.putExtra("has_text", true);
                                    schedule.putExtra("text", reply.getText().toString());
                                }
                                startActivity(schedule);
                                finish();
                                break;
                            case VIEW_QUEUE:
                                final String[] queued = QueuedDataSource.getInstance(context).getQueuedTweets(currentAccount);
                                if (queued.length > 0) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setItems(queued, new DialogInterface.OnClickListener() {
                                        public void onClick(final DialogInterface dialog, final int item) {

                                            new AlertDialog.Builder(context)
                                                    .setTitle(context.getResources().getString(R.string.keep_queued_tweet))
                                                    .setMessage(queued[item])
                                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                        }
                                                    })
                                                    .setNegativeButton(R.string.delete_draft, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            QueuedDataSource.getInstance(context).deleteQueuedTweet(queued[item]);
                                                            dialogInterface.dismiss();
                                                        }
                                                    })
                                                    .create()
                                                    .show();

                                            dialog.dismiss();
                                        }
                                    });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                } else {
                                    Toast.makeText(context, R.string.no_queued, Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }

                        return false;
                    }
                });

                menu.show();
            }
        });

        final ImageButton convertToDM = (ImageButton) findViewById(R.id.dm_button);
        convertToDM.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(context, R.string.menu_direct_message, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        convertToDM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent dm = new Intent(context, ComposeDMActivity.class);
                startActivity(dm);
                overridePendingTransition(0,0);
                finish();
                overridePendingTransition(0,0);
            }
        });

        final ImageButton location = (ImageButton) findViewById(R.id.location);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!addLocation) {
                    sharedPrefs.edit().putBoolean("share_location", true).apply();
                    addLocation = true;

                    location.setColorFilter(settings.themeColors.accentColor);
                } else {
                    sharedPrefs.edit().putBoolean("share_location", false).apply();
                    addLocation = false;

                    location.clearColorFilter();
                }
            }
        });

        if (sharedPrefs.getBoolean("share_location", false)) {
            location.performClick();
        }

        if (!settings.useEmoji) {
            emojiButton.setVisibility(View.GONE);
        } else {
            emojiKeyboard.setAttached((FontPrefEditText) reply);

            reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (emojiKeyboard.isShowing()) {
                        emojiKeyboard.setVisibility(false);

                        TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.emoji_button_changing});
                        int resource = a.getResourceId(0, 0);
                        a.recycle();
                        emojiButton.setImageResource(resource);
                    }
                }
            });

            emojiButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (emojiKeyboard.isShowing()) {
                        emojiKeyboard.setVisibility(false);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                InputMethodManager imm = (InputMethodManager)getSystemService(
                                        INPUT_METHOD_SERVICE);
                                imm.showSoftInput(reply, 0);
                            }
                        }, 250);

                        TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.emoji_button_changing});
                        int resource = a.getResourceId(0, 0);
                        a.recycle();
                        emojiButton.setImageResource(resource);
                    } else {
                        InputMethodManager imm = (InputMethodManager)getSystemService(
                                INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(reply.getWindowToken(), 0);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                emojiKeyboard.setVisibility(true);
                            }
                        }, 250);

                        TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.keyboard_button_changing});
                        int resource = a.getResourceId(0, 0);
                        a.recycle();
                        emojiButton.setImageResource(resource);
                    }
                }
            });
        }
    }

    public void findGif() {
        Intent gif = new Intent(context, GiphySearch.class);
        startActivityForResult(gif, FIND_GIF);
    }

    public void attachImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(R.array.attach_options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if(item == 0) { // take picture
                    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(Environment.getExternalStorageDirectory() + "/Talon/", "photoToTweet.jpg");

                    if (!f.exists()) {
                        try {
                            f.getParentFile().mkdirs();
                            f.createNewFile();
                        } catch (IOException e) {

                        }
                    }

                    captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    try {
                        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, IOUtils.getImageContentUri(context, f));
                        startActivityForResult(captureIntent, CAPTURE_IMAGE);
                    } catch (Exception e) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Toast.makeText(ComposeActivity.this, "Have you given Talon the storage permission?", Toast.LENGTH_LONG).show();
                        }
                    }

                } else if (item == 1) { // attach picture
                    try {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, SELECT_PHOTO);
                    } catch (Exception e) {
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(Intent.createChooser(photoPickerIntent,
                                "Select Picture"), SELECT_PHOTO);
                    }
                } else if (item == 2) {
                    Toast.makeText(ComposeActivity.this, "GIFs must be less than 5 MB", Toast.LENGTH_SHORT).show();

                    try {
                        Intent gifIntent = new Intent();
                        gifIntent.setType("image/gif");
                        gifIntent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(gifIntent, SELECT_GIF);
                    } catch (Exception e) {
                        Intent gifIntent = new Intent();
                        gifIntent.setType("image/gif");
                        gifIntent.setAction(Intent.ACTION_PICK);
                        startActivityForResult(gifIntent, SELECT_GIF);
                    }
                } else if (item == 3) {
                    Toast.makeText(ComposeActivity.this, "Videos must be less than 15 MB", Toast.LENGTH_SHORT).show();

                    try {
                        Intent gifIntent = new Intent();
                        gifIntent.setType("surfaceView/mp4");
                        gifIntent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(gifIntent, SELECT_VIDEO);
                    } catch (Exception e) {
                        Intent gifIntent = new Intent();
                        gifIntent.setType("surfaceView/mp4");
                        gifIntent.setAction(Intent.ACTION_PICK);
                        startActivityForResult(gifIntent, SELECT_VIDEO);
                    }
                }
            }
        });

        builder.create().show();
    }

    public void setUpReplyText() {
        // for failed notification
        if (getIntent().getStringExtra("failed_notification_text") != null) {
            reply.setText(getIntent().getStringExtra("failed_notification_text"));
            reply.setSelection(reply.getText().length());
        }

        String to = getIntent().getStringExtra("user") + (isDM ? "" : " ");

        if ((!to.equals("null ") && !isDM) || (isDM && !to.equals("null"))) {
            if(!isDM) {
                Log.v("username_for_noti", "to place: " + to);
                reply.setText(to);
                reply.setSelection(reply.getText().toString().length());
            } else {
                contactEntry.setText(to);
                reply.requestFocus();
            }

            sharedPrefs.edit().putString("draft", "").apply();
        }

        notiId = getIntent().getLongExtra("id", 0);
        replyText = getIntent().getStringExtra("reply_to_text");

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        }
    }

    public boolean doneClick() {

        if (emojiKeyboard.isShowing()) {
            emojiKeyboard.setVisibility(false);

            TypedArray a = getTheme().obtainStyledAttributes(new int[]{R.attr.emoji_button});
            int resource = a.getResourceId(0, 0);
            a.recycle();
            emojiButton.setImageResource(resource);
        }

        EditText editText = (EditText) findViewById(R.id.tweet_content);
        final String status = editText.getText().toString();

        if (!Utils.hasInternetConnection(context) && !status.isEmpty() && imagesAttached == 0) {
            // we are going to queue this tweet to send for when they get a connection
            QueuedDataSource.getInstance(context).createQueuedTweet(status, currentAccount);
            Toast.makeText(context, R.string.tweet_queued, Toast.LENGTH_SHORT).show();
            return true;
        } else if (!Utils.hasInternetConnection(context) && imagesAttached > 0) {
            // we only queue tweets without pictures
            Toast.makeText(context, R.string.only_queue_no_pic, Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check for blank text
        if (Integer.parseInt(charRemaining.getText().toString()) >= 0 || settings.twitlonger) {
            // update status
            if (Integer.parseInt(charRemaining.getText().toString()) < 0) {
                onBackPressed();
                doneClicked = true;
                sendStatus(status, Integer.parseInt(charRemaining.getText().toString()));
                return true;
            } else {
                doneClicked = true;
                sendStatus(status, Integer.parseInt(charRemaining.getText().toString()));
                return true;
            }
        } else {
            if (editText.getText().length() + (attachedUri.equals("") ? 0 : 22) <= 140) {
                // EditText is empty
                Toast.makeText(context, context.getResources().getString(R.string.error_sending_tweet), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.tweet_to_long), Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    }

    public void sendStatus(String status, int length) {
        new updateTwitterStatus(reply.getText().toString(), length).execute(status);
    }

    @Override
    public void onPause() {
        sharedPrefs.edit().putString("draft", "").apply();
        try {
            if (!(doneClicked || discardClicked)) {
                QueuedDataSource.getInstance(context).createDraft(reply.getText().toString(), currentAccount);
            }
        } catch (Exception e) {
            // it is a direct message
        }

        super.onPause();
    }

}