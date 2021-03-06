package cz.csas.startup.FBPoc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import com.facebook.FacebookException;
import com.facebook.model.GraphUser;
import com.facebook.widget.FriendPickerFragment;
import com.facebook.widget.PickerFragment;
import cz.csas.startup.FBPoc.utils.Utils;

import java.util.ArrayList;

/**
 * Created by cen29414 on 18.4.2014.
 */
public class PickerActivity extends FragmentActivity {
    public static final Uri FRIEND_PICKER = Uri.parse("picker://friend");
    public static final String MULTI_SELECTION = "multiSelection";
    public static final String TITLE = "TITLE";

    private FriendPickerFragment friendPickerFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!((Friends24Application) getApplication()).getFriends24Context().isAppLogged()) {
            finish();
            Utils.redirectToLogin(this);
            return;
        }
        setContentView(R.layout.pickers);

        Bundle args = getIntent().getExtras();
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragmentToShow = null;
        Uri intentUri = getIntent().getData();

        if (FRIEND_PICKER.equals(intentUri)) {
            final boolean multiSelect = args.getBoolean(MULTI_SELECTION, false);
            if (savedInstanceState == null) {
                friendPickerFragment = new FriendPickerFragment(args);
                friendPickerFragment.setMultiSelect(multiSelect);
                friendPickerFragment.setDoneButtonText(getResources().getString(R.string.OK));
                friendPickerFragment.setTitleText(args.getString(TITLE, getResources().getString(R.string.choose_friend)));
            } else {
                friendPickerFragment =
                        (FriendPickerFragment) manager.findFragmentById(R.id.picker_fragment);
            }
            // Set the listener to handle errors
            friendPickerFragment.setOnErrorListener(new PickerFragment.OnErrorListener() {
                @Override
                public void onError(PickerFragment<?> fragment,
                                    FacebookException error) {
                    PickerActivity.this.onError(error);
                }
            });
            // Set the listener to handle button clicks
            friendPickerFragment.setOnDoneButtonClickedListener(
                    new PickerFragment.OnDoneButtonClickedListener() {
                        @Override
                        public void onDoneButtonClicked(PickerFragment<?> fragment) {
                            Friends24Application application = (Friends24Application) getApplication();
                            if (application.getFriends24Context().getSelectedFriends() == null) {
                                application.getFriends24Context().setSelectedFrieds(new ArrayList<GraphUser>());
                            }
                            if (multiSelect) {
                                application.getFriends24Context().getSelectedFriends().addAll(friendPickerFragment.getSelection());
                            }
                            else {
                                application.getFriends24Context().setSelectedFrieds(friendPickerFragment.getSelection());
                            }
                            application.getFriends24Context().setNewlySelectedFrieds(friendPickerFragment.getSelection());
                            application.saveSessionToPreferences();
                            finishActivity();
                        }
                    });
            fragmentToShow = friendPickerFragment;

        } else {
            // Nothing to do, finish
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        manager.beginTransaction()
                .replace(R.id.picker_fragment, fragmentToShow)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FRIEND_PICKER.equals(getIntent().getData())) {
            try {
                friendPickerFragment.loadData(false);
            } catch (Exception ex) {
                onError(ex);
            }
        }
    }

    private void onError(Exception error) {
        onError(error.getLocalizedMessage(), false);
    }

    private void onError(String error, final boolean finishActivity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error_dialog_title).
                setMessage(error).
                setPositiveButton(R.string.error_dialog_button_text,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (finishActivity) {
                                    finishActivity();
                                }
                            }
                        });
        builder.show();
    }

    private void finishActivity() {
        setResult(RESULT_OK);
        finish();
    }


}
