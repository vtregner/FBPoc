package cz.csas.startup.FBPoc.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by cen29414 on 19.5.2014.
 */
public class Collection implements Parcelable {
    private String id;
    private Long collectionAccount;
    private String name;
    private BigDecimal targetAmount;
    private String currency;
    private String description;
    private Bitmap image;
    private boolean hasImage;
    private String link;
    private Date dueDate;
    private Date created;
    private List<FacebookCollectionParticipant> fbParticipants;
    private List<EmailCollectionParticipant> emailParticipants;

    public Long getCollectionAccount() {
        return collectionAccount;
    }

    public void setCollectionAccount(Long collectionAccount) {
        this.collectionAccount = collectionAccount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public List<FacebookCollectionParticipant> getFbParticipants() {
        return fbParticipants;
    }

    public void setFbParticipants(List<FacebookCollectionParticipant> fbParticipants) {
        this.fbParticipants = fbParticipants;
    }

    public List<EmailCollectionParticipant> getEmailParticipants() {
        return emailParticipants;
    }

    public void setEmailParticipants(List<EmailCollectionParticipant> emailParticipants) {
        this.emailParticipants = emailParticipants;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isHasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        try {
            object.put("dueDate", getDueDate().getTime());
            object.put("collectionAccount", getCollectionAccount());
            if (getTargetAmount() != null) object.put("targetAmount", getTargetAmount());
            object.put("currency", getCurrency());
            object.put("name", getName());
            if (getDescription() != null) object.put("description", getDescription());
            if (getLink() != null) object.put("link", getLink());
            object.put("hasImage", isHasImage());

            if (getFbParticipants() != null) {
                JSONArray fbp = new JSONArray();
                for (FacebookCollectionParticipant participant : fbParticipants) {
                    fbp.put(participant.toJson());
                }
                object.put("collectionFBParticipants", fbp);
            }

            if (getEmailParticipants() != null) {
                JSONArray ebp = new JSONArray();
                for (EmailCollectionParticipant participant : emailParticipants) {
                    ebp.put(participant.toJson());
                }
                object.put("collectionEmailParticipants", ebp);
            }

            return object.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static Collection fromJson(JSONObject jcollection) throws JSONException {
        Collection collection = new Collection();
        collection.setId(jcollection.getString("id"));
        collection.setCreated(new Date(jcollection.getLong("created")));
        collection.setDueDate(new Date(jcollection.getLong("dueDate")));
        collection.setCollectionAccount(jcollection.getLong("collectionAccount"));
        if (!jcollection.isNull("targetAmount")) collection.setTargetAmount(new BigDecimal(jcollection.getString("targetAmount")));
        collection.setCurrency(jcollection.getString("currency"));
        collection.setName(jcollection.getString("name"));
        if (!jcollection.isNull("description")) collection.setDescription(jcollection.getString("description"));

        if (!jcollection.isNull("image")) {
            byte[] bytes = Base64.decode(jcollection.getString("image"), Base64.NO_WRAP);
            collection.setImage(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
        }

        collection.setHasImage(jcollection.getBoolean("hasImage"));

        if (!jcollection.isNull("link")) collection.setLink(jcollection.getString("link"));

        if (!jcollection.isNull("collectionFBParticipants")) {
            List<FacebookCollectionParticipant> participants = new ArrayList<FacebookCollectionParticipant>();
            collection.setFbParticipants(participants);
            JSONArray jparticipants = jcollection.getJSONArray("collectionFBParticipants");
            for (int j=0; j< jparticipants.length(); j++) {
                JSONObject jparticipant = jparticipants.getJSONObject(j);
                FacebookCollectionParticipant participant = new FacebookCollectionParticipant();
                participant.setId(jparticipant.getLong("id"));
                participant.setFbUserId(jparticipant.getString("fbUserId"));
                participant.setFbUserName(jparticipant.getString("fbUserName"));
                if (!jparticipant.isNull("amount")) participant.setAmount(new BigDecimal(jparticipant.getString("amount")));
                participant.setStatus(CollectionParticipant.Status.valueOf(jparticipant.getInt("status")));
                participants.add(participant);
            }
        }

        if (!jcollection.isNull("collectionEmailParticipants")) {
            List<EmailCollectionParticipant> participants = new ArrayList<EmailCollectionParticipant>();
            collection.setEmailParticipants(participants);
            JSONArray jparticipants = jcollection.getJSONArray("collectionEmailParticipants");
            for (int j=0; j< jparticipants.length(); j++) {
                JSONObject jparticipant = jparticipants.getJSONObject(j);
                EmailCollectionParticipant participant = new EmailCollectionParticipant();
                participant.setId(jparticipant.getLong("id"));
                participant.setEmail(jparticipant.getString("email"));
                if (!jparticipant.isNull("amount")) participant.setAmount(new BigDecimal(jparticipant.getString("amount")));
                participant.setStatus(CollectionParticipant.Status.valueOf(jparticipant.getInt("status")));
                participants.add(participant);
            }
        }

        return collection;
    }

    public BigDecimal getCurrentCollectedAmount() {
        BigDecimal sum = BigDecimal.ZERO;
        if (getFbParticipants() != null) {
            for (FacebookCollectionParticipant participant : fbParticipants) {
                if (participant.getStatus().equals(CollectionParticipant.Status.DONE)) {
                    sum = sum.add(participant.getAmount());
                }
            }
            for (EmailCollectionParticipant participant : emailParticipants) {
                if (participant.getStatus().equals(CollectionParticipant.Status.DONE)) {
                    sum = sum.add(participant.getAmount());
                }
            }
        }
        return sum;
    }

    public int getNumberOfParticipants() {
        int num = 0;
        if (getFbParticipants() != null) num += getFbParticipants().size();
        if (getEmailParticipants() != null) num += getEmailParticipants().size();
        return num;
    }

    public Status getCurrentCollectionProgress() {
        boolean allPaid = true;
        boolean allRefused = true;
        if (getEmailParticipants() != null) {
            for (CollectionParticipant participant : emailParticipants) {
                if (participant.getStatus() != CollectionParticipant.Status.REFUSED) {
                    allRefused = false;
                }
                if (participant.getStatus() != CollectionParticipant.Status.DONE) {
                    allPaid = false;
                }
            }
        }
        if (allPaid && getFbParticipants() != null) {
            for (CollectionParticipant participant : fbParticipants) {
                if (participant.getStatus() != CollectionParticipant.Status.REFUSED) {
                    allRefused = false;
                }
                if (participant.getStatus() != CollectionParticipant.Status.DONE) {
                    allPaid = false;
                }
            }
        }

        if (getDueDate().before(new Date()))  {
            // collection finished
            return allPaid ? Status.DONE : Status.EXPIRED;
        }
        else {
            return allPaid ? Status.DONE : (allRefused ? Status.EXPIRED : Status.INPROGRESS);
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeValue(this.collectionAccount);
        dest.writeString(this.name);
        dest.writeSerializable(this.targetAmount);
        dest.writeString(this.currency);
        dest.writeString(this.description);
        //dest.writeParcelable(this.image, 0);
        dest.writeString(this.link);
        dest.writeLong(dueDate != null ? dueDate.getTime() : -1);
        dest.writeLong(created != null ? created.getTime() : -1);
        dest.writeTypedList(fbParticipants);
        dest.writeTypedList(emailParticipants);
        dest.writeByte(hasImage ? (byte)1 : 0);
    }

    public Collection() {
    }

    private Collection(Parcel in) {
        this.id = in.readString();
        this.collectionAccount = (Long) in.readValue(Long.class.getClassLoader());
        this.name = in.readString();
        this.targetAmount = (BigDecimal) in.readSerializable();
        this.currency = in.readString();
        this.description = in.readString();
        //this.image = in.readParcelable(Bitmap.class.getClassLoader());
        this.link = in.readString();
        long tmpDueDate = in.readLong();
        this.dueDate = tmpDueDate == -1 ? null : new Date(tmpDueDate);
        long tmpCreated = in.readLong();
        this.created = tmpCreated == -1 ? null : new Date(tmpCreated);
        fbParticipants = new ArrayList<FacebookCollectionParticipant>();
        emailParticipants = new ArrayList<EmailCollectionParticipant>();
        in.readTypedList(fbParticipants, FacebookCollectionParticipant.CREATOR);
        in.readTypedList(emailParticipants, EmailCollectionParticipant.CREATOR);
        this.hasImage = in.readByte() == 1;
    }

    public static Parcelable.Creator<Collection> CREATOR = new Parcelable.Creator<Collection>() {
        public Collection createFromParcel(Parcel source) {
            return new Collection(source);
        }

        public Collection[] newArray(int size) {
            return new Collection[size];
        }
    };

    public enum Status {
        DONE(0), INPROGRESS(1), EXPIRED(2);

        private int value;

        Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
