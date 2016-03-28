package com.koolearn.klibrary.core.view;

import com.koolearn.android.util.LogUtil;

public interface ZLViewEnums {
    public enum PageIndex {
        previous, current, next;

        public PageIndex getNext() {
            LogUtil.i13("getPrevious" + this);
            switch (this) {
                case previous:
                    return current;
                case current:
                    return next;
                default:
                    return null;
            }
        }

        public PageIndex getPrevious() {
            LogUtil.i13("getPrevious" + this);
            switch (this) {
                case next:
                    return current;
                case current:
                    return previous;
                default:
                    return null;
            }
        }
    }

    public enum Direction {
        leftToRight(true), rightToLeft(true), up(false), down(false);

        public final boolean IsHorizontal;

        Direction(boolean isHorizontal) {
            IsHorizontal = isHorizontal;
        }
    }

    public enum Animation {
        none, curl, slide, slideOldStyle, shift
    }
}
