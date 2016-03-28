package com.koolearn.android.kooreader.libraryService;

import android.os.Parcel;
import android.os.Parcelable;

import com.koolearn.android.util.LogInfo;
import com.koolearn.klibrary.text.view.ZLTextFixedPosition;
import com.koolearn.klibrary.text.view.ZLTextPosition;

public final class PositionWithTimestamp implements Parcelable {
	public final int ParagraphIndex;
	public final int ElementIndex;
	public final int CharIndex;
	public final long Timestamp;

	public PositionWithTimestamp(ZLTextPosition pos) {

		this(
		    pos.getParagraphIndex(),
			pos.getElementIndex(),
			pos.getCharIndex(),
			(pos instanceof ZLTextFixedPosition.WithTimestamp)
				? ((ZLTextFixedPosition.WithTimestamp)pos).Timestamp : -1
		);
	}

	private PositionWithTimestamp(int paragraphIndex, int elementIndex, int charIndex, long stamp) {
		LogInfo.I("");

		ParagraphIndex = paragraphIndex;
		ElementIndex = elementIndex;
		CharIndex = charIndex;
		Timestamp = stamp;
	}

	//y 写下标当前时间 目录页数 阅读百分比
	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		LogInfo.I("");

		parcel.writeInt(ParagraphIndex);
		parcel.writeInt(ElementIndex);
		parcel.writeInt(CharIndex);
		parcel.writeLong(Timestamp);
	}

	@Override
	public int describeContents() {
		LogInfo.I("");

		return 0;
	}

	public static final Parcelable.Creator<PositionWithTimestamp> CREATOR =
		new Parcelable.Creator<PositionWithTimestamp>() {
			public PositionWithTimestamp createFromParcel(Parcel parcel) {
				LogInfo.I("");

				return new PositionWithTimestamp(parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readLong());
			}

			public PositionWithTimestamp[] newArray(int size) {
				return new PositionWithTimestamp[size];
			}
		};
}
