/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package com.koolearn.kooreader.kooreader.options;

import com.koolearn.klibrary.core.options.ZLBooleanOption;
import com.koolearn.klibrary.core.options.ZLColorOption;
import com.koolearn.klibrary.core.options.ZLEnumOption;
import com.koolearn.klibrary.core.util.ZLColor;
import com.koolearn.kooreader.kooreader.KooView;

public class ImageOptions {
	public final ZLColorOption ImageViewBackground;

	public final ZLEnumOption<KooView.ImageFitting> FitToScreen;
	public static enum TapActionEnum {
		doNothing, selectImage, openImageView
	}
	public final ZLEnumOption<TapActionEnum> TapAction;
	public final ZLBooleanOption MatchBackground;

	public ImageOptions() {
		ImageViewBackground =
			new ZLColorOption("Colors", "ImageViewBackground", new ZLColor(255, 255, 255));
		FitToScreen =
			new ZLEnumOption<KooView.ImageFitting>("Options", "FitImagesToScreen", KooView.ImageFitting.covers);
		TapAction =
			new ZLEnumOption<TapActionEnum>("Options", "ImageTappingAction", TapActionEnum.openImageView);
		MatchBackground =
			new ZLBooleanOption("Colors", "ImageMatchBackground", true);
	}
}
