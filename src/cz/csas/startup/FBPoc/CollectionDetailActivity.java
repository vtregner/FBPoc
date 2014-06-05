package cz.csas.startup.FBPoc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.facebook.android.Util;
import cz.csas.startup.FBPoc.model.*;
import cz.csas.startup.FBPoc.service.AsyncTaskResult;
import cz.csas.startup.FBPoc.service.GetCollectionImageTask;
import cz.csas.startup.FBPoc.service.OnTaskCompleteListener;
import cz.csas.startup.FBPoc.service.SendFBMessageNotifyCollectionTask;
import cz.csas.startup.FBPoc.utils.Utils;
import cz.csas.startup.FBPoc.widget.RoundedProfilePictureView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by cen29414 on 19.5.2014.
 */
public class CollectionDetailActivity extends FbAwareActivity {
    private static final String TAG = "Friends24";

    Collection collection;
    ProgressBar imageProgressBar;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection_detail);
        collection = getIntent().getParcelableExtra("data");


        TextView accountRow1 = (TextView) findViewById(R.id.accountRow1);
        TextView accountRow2 = (TextView) findViewById(R.id.accountRow2);
        Account account = ((Friends24Application) getApplication()).getAccount(collection.getCollectionAccount());
        accountRow1.setText(AccountsAdapter.getAccountRow1(account));
        accountRow2.setText(AccountsAdapter.getAccountRow2(account));

        final TextView collectionHeader1 = (TextView) findViewById(R.id.collectionHeader1);
        TextView collectionHeader2 = (TextView) findViewById(R.id.collectionHeader2);
        collectionHeader1.setText(collection.getName().toUpperCase());
        SimpleDateFormat sfd = new SimpleDateFormat("dd.MM.yyyy");
        collectionHeader2.setText(Utils.getFormattedAmount(collection.getTargetAmount(), collection.getCurrency()) + " do " + sfd.format(collection.getDueDate()));
        image = (ImageView) findViewById(R.id.collectionImage);
        imageProgressBar = (ProgressBar) findViewById(R.id.imageProgressBar);
        if (collection.getImage() != null) {
            image.setImageDrawable(new BitmapDrawable(getResources(), collection.getImage()));
            image.setVisibility(View.VISIBLE);
        }
        else {
            imageProgressBar.setVisibility(collection.isHasImage() ? View.VISIBLE : View.GONE);
            image.setVisibility(View.GONE);

        }

        TextView descriptionView = (TextView) findViewById(R.id.collectionDescription);
        descriptionView.setText(collection.getDescription());
        TextView linkView = (TextView) findViewById(R.id.collectionLink);
        linkView.setVisibility(collection.getLink() != null ? View.VISIBLE : View.GONE);
        if (collection.getLink() != null) {
            linkView.setText(Html.fromHtml("<a href=\"" + collection.getLink() + "\">" + getString(R.string.collectionLinkInfo) + "</a>"));
            linkView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(collection.getTargetAmount().intValue());
        BigDecimal currentCollectedAmount = collection.getCurrentCollectedAmount();
        progressBar.setProgress(currentCollectedAmount.intValue());

        TextView currentAmount = (TextView) findViewById(R.id.currentAmount);
        currentAmount.setText(Utils.getFormattedAmount(currentCollectedAmount,collection.getCurrency()));
        TextView targetAmount = (TextView) findViewById(R.id.targetAmount);
        targetAmount.setText("z " + Utils.getFormattedAmount(collection.getTargetAmount(),collection.getCurrency()));

        TextView numberOfParticipants = (TextView) findViewById(R.id.numberOfParticipants);
        numberOfParticipants.setText("(" + collection.getNumberOfParticipants() + ")");

        if (savedInstanceState == null) {
            appendParticipants(collection);
        }

        ViewGroup imageContainer = (ViewGroup) findViewById(R.id.collectionImageContainer);
        if (collection.isHasImage()) {
            GetCollectionImageTask task = new GetCollectionImageTask(this, collection.getId(), Utils.convertToPx(this, imageContainer.getLayoutParams().height)) {
                @Override
                protected void onPostExecute(AsyncTaskResult<byte[]> result) {
                    super.onPostExecute(result);
                    imageProgressBar.setVisibility(View.GONE);
                    if (result.getStatus() == AsyncTaskResult.Status.OK) {
                        image.setImageBitmap(getBitmap());
                        collection.setImage(getBitmap());
                    }
                    image.setVisibility(getBitmap() != null ? View.VISIBLE : View.GONE);
                }
            };
            task.execute();
        }
        else imageContainer.setVisibility(View.GONE);

        View btnNotify = findViewById(R.id.btnNotify);
        TextView collectionExpiredView = (TextView) findViewById(R.id.collectionExpiredNotification);
        boolean expired = collection.getDueDate().before(new Date());
        if (!expired) {
            if (canNotifyParticipants(collection.getFbParticipants()) || canNotifyParticipants(collection.getEmailParticipants())) {
                btnNotify.setVisibility(View.VISIBLE);
                if (canNotifyParticipants(collection.getFbParticipants())) {
                    initSmack();
                }
            }
            else {
                btnNotify.setVisibility(View.GONE);
            }
        }
        else btnNotify.setVisibility(View.GONE);

        collectionExpiredView.setVisibility(expired ? View.VISIBLE : View.GONE);
        if (expired) {
            sfd = new SimpleDateFormat("dd.MM.");
            collectionExpiredView.setText(collectionExpiredView.getText() + " " + sfd.format(collection.getDueDate()));
        }
    }

    private boolean canNotifyParticipants(List<? extends CollectionParticipant> participants) {
        boolean can = false;
        if (participants != null) {
            for (CollectionParticipant participant : participants) {
                if (participant.getStatus() == CollectionParticipant.Status.PENDING || participant.getStatus() == CollectionParticipant.Status.ACCEPTED) {
                    can = true;
                    break;
                }
            }
        }
        return can;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        appendParticipants(collection);
    }


    private void appendParticipants(Collection collection) {
        TableLayout participantsList = (TableLayout) findViewById(R.id.participantList);
        LayoutInflater inflater = LayoutInflater.from(this);
        int row=1;
        participantsList.removeAllViews();

        if (collection.getFbParticipants() != null) {
            for (FacebookCollectionParticipant participant : collection.getFbParticipants()) {
                View view = inflater.inflate(R.layout.participants_row, null);

                RoundedProfilePictureView profilePictureView = (RoundedProfilePictureView) view.findViewById(R.id.participantPic);
                profilePictureView.setProfileId(participant.getFbUserId());
                profilePictureView.setVisibility(View.VISIBLE);

                TextView participantName = (TextView) view.findViewById(R.id.participantName);
                participantName.setText(Utils.getShortFacebookName(participant.getFbUserName()));

                row = appendParticipantCommonValues(collection, row, participant, view);

                participantsList.addView(view);
            }

        }

        if (collection.getEmailParticipants() != null) {
            for (EmailCollectionParticipant participant : collection.getEmailParticipants()) {
                View view = inflater.inflate(R.layout.participants_row, null);

                RoundedProfilePictureView profilePictureView = (RoundedProfilePictureView) view.findViewById(R.id.participantPic);
                profilePictureView.setVisibility(View.GONE);

                TextView participantName = (TextView) view.findViewById(R.id.participantName);
                participantName.setText(participant.getEmail());

                row = appendParticipantCommonValues(collection, row, participant, view);

                participantsList.addView(view);
            }
        }

    }

    private int appendParticipantCommonValues(Collection collection, int row, CollectionParticipant participant, View view) {
        TextView amount = (TextView) view.findViewById(R.id.amount);
        if (participant.getAmount() != null) {
            amount.setText(Utils.getFormattedAmount(participant.getAmount(),collection.getCurrency()));
        }

        ImageView status = (ImageView) view.findViewById(R.id.participantStatus);
        if (participant.getStatus().equals(CollectionParticipant.Status.DONE)) {
            status.setImageDrawable(getResources().getDrawable(R.drawable.paymentaccepted));
        }
        else if (participant.getStatus().equals(CollectionParticipant.Status.REFUSED)) {
            status.setImageDrawable(getResources().getDrawable(R.drawable.paymentrefused));
        }
        else if (participant.getStatus().equals(CollectionParticipant.Status.ACCEPTED)) {
            status.setImageDrawable(getResources().getDrawable(R.drawable.paymentpending));
        }
        else {
            status.setImageDrawable(getResources().getDrawable(R.drawable.collectionwithoutreaction));
        }

        Drawable background = (row++%2 == 0) ? getResources().getDrawable(R.color.cell_even) : getResources().getDrawable(R.color.cell_odd);
        view.findViewById(R.id.rowMarkColor).setBackground(background);
        return row;
    }

    public void onNotify(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.notifyParticipantsTitle))
                .setMessage(getString(R.string.notifyParticipantsConfirmText))
                .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (canNotifyParticipants(collection.getFbParticipants())) {
                            new SendFBMessageNotifyCollectionTask(CollectionDetailActivity.this, null, new OnTaskCompleteListener<Void>() {
                                @Override
                                public void onTaskComplete(Void aVoid) {
                                    Utils.showToast(CollectionDetailActivity.this, R.string.collectionNotificationSuccess);
                                }

                                @Override
                                public void onTaskError(Throwable throwable) {
                                    Utils.showToast(CollectionDetailActivity.this, R.string.collectionNotificationSuccess);
                                }
                            }).execute(collection);
                        }
                    }
                })
                .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    public void onImageDetail(View view) {
        if (collection.getImage() != null) {
            Intent intent = new Intent(this, ImageDetailActivity.class);
            intent.putExtra(ImageDetailActivity.COLLECTION_ID, collection.getId());
            startActivity(intent);
        }
    }

    private String saveImageLocal(byte[] data, String collectionId) {
        final File root = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File file = new File(root, "temp_" + collectionId + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
            return file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Cannot save image to local storage", e);
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Cannot save image to local storage", e);
            return null;
        }
    }
}
