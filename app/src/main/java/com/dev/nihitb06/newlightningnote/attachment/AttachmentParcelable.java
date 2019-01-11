package com.dev.nihitb06.newlightningnote.attachment;

import android.os.Parcel;
import android.os.Parcelable;

public class AttachmentParcelable implements Parcelable {

    private String uri;
    private int type;

    public static final Parcelable.Creator<AttachmentParcelable> CREATOR = new Parcelable.Creator<AttachmentParcelable>() {
        @Override
        public AttachmentParcelable createFromParcel(Parcel source) {
            return new AttachmentParcelable(source);
        }

        @Override
        public AttachmentParcelable[] newArray(int size) {
            return new AttachmentParcelable[0];
        }
    };

    public AttachmentParcelable(String uri, int type) {
        this.uri = uri;
        this.type = type;
    }

    private AttachmentParcelable(Parcel source) {
        this.uri = source.readString();
        this.type = source.readInt();
    }

    public String getUri() {
        return uri;
    }

    public int getType() {
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uri);
        dest.writeInt(type);
    }
}
