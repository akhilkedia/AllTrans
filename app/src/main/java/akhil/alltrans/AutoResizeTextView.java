/*
 * Copyright 2017 Akhil Kedia
 * This file is part of AllTrans.
 *
 * AllTrans is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AllTrans is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AllTrans. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package akhil.alltrans;

import android.text.TextPaint;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.graphics.RectF;
import android.text.method.TransformationMethod;
import android.util.TypedValue;
import android.widget.TextView;
import android.text.method.SingleLineTransformationMethod;

public class AutoResizeTextView {

    private static final int NO_LINE_LIMIT = -1;
    private static float mMinTextSize = 10;

    private static int getMaxLines(TextView view) {
        int maxLines = NO_LINE_LIMIT; // No limit (Integer.MAX_VALUE also means no limit)

        TransformationMethod method = view.getTransformationMethod();
        if (method instanceof SingleLineTransformationMethod) {
            maxLines = 1;
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // setMaxLines() and getMaxLines() are only available on android-16+
            maxLines = view.getMaxLines();
        }
        return maxLines;
    }

    public static void adjustTextSize(TextView tv, CharSequence text) {
//        if (!text.toString().equals("Public playground")){
//            return;
//        }
        RectF availableSpaceRect = new RectF();
        availableSpaceRect.bottom = tv.getHeight() - tv.getCompoundPaddingBottom()
                - tv.getCompoundPaddingTop();
        availableSpaceRect.right = tv.getMeasuredWidth() - tv.getCompoundPaddingLeft()
                - tv.getCompoundPaddingRight();
        if (availableSpaceRect.bottom <= 0 || availableSpaceRect.right <=0) {
            return;
        }
        utils.debugLog("Available Space - " + availableSpaceRect.toShortString());
        int idealSize = binarySearch(tv, text, availableSpaceRect);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, idealSize);
    }

    private static int binarySearch(TextView tv, CharSequence text, RectF availableSpaceRect) {
        int lastBest = 10;
        int lo = 10;
//        Get min and max wherever applicable
        int hi = 1000 - 1;
        int mid = 0;
        while (lo <= hi) {
            mid = (lo + hi) >>> 1;
            int midValCmp = onTestSize(tv, text, mid, availableSpaceRect);
            if (midValCmp < 0) {
                utils.debugLog("midValCmp < 0 " + mid);
                lastBest = lo;
                lo = mid + 1;
            } else if (midValCmp > 0) {
                utils.debugLog("midValCmp > 0 " + mid);
                hi = mid - 1;
                lastBest = hi;
            } else {
//                Will never come here
                return mid;
            }
        }
        // Make sure to return the last best.
        // This is what should always be returned.
        return lastBest;

    }

    private static int onTestSize(TextView tv, CharSequence textSequence, int suggestedSize, RectF availableSpaceRect) {
//        Move this to object
        TextPaint mPaint = new TextPaint(tv.getPaint());
        RectF mTextRect = new RectF();
        mPaint.setTextSize(suggestedSize);
        boolean singleline = getMaxLines(tv) == 1;
        if (singleline) {
            mTextRect.bottom = mPaint.getFontSpacing();
            mTextRect.right = mPaint.measureText(textSequence.toString());
        } else {
            StaticLayout layout = new StaticLayout(textSequence, mPaint,
                    (int) availableSpaceRect.right, Layout.Alignment.ALIGN_NORMAL, 1.0f,
                    0.0f, true);

            // Return early if we have more lines
            if (getMaxLines(tv) != NO_LINE_LIMIT
                    && layout.getLineCount() > getMaxLines(tv)) {
                utils.debugLog("return 1 too big from no line ");
                return 1;
            }
            mTextRect.bottom = layout.getHeight();
            int maxWidth = 0;
            for (int i = 0; i < layout.getLineCount(); i++) {
                if (maxWidth < layout.getLineWidth(i)) {
                    maxWidth = (int) layout.getLineWidth(i);
                }
            }
            mTextRect.right = maxWidth;
        }

        mTextRect.offsetTo(0, 0);
        utils.debugLog("got computed Size as " + mTextRect.toShortString());
        if (availableSpaceRect.contains(mTextRect)) {

            // May be too small, don't worry we will find the best match
            utils.debugLog("return -1 too small from end ");
            return -1;
        } else {
            utils.debugLog("return 1 too big from from end ");
            // too big
            return 1;
        }
    }

//    private int efficientTextSizeSearch(TextView tv, CharSequence text, int heightLimit, int widthLimit) {
////        if (!mEnableSizeCache) {
////            return binarySearch(start, end, sizeTester, availableSpace);
////        }
////        int key = getText().toString().length();
////        int size = mTextCachedSizes.get(key);
////        if (size != 0) {
////            return size;
////        }
////        size = binarySearch(start, end, sizeTester, availableSpace);
////        mTextCachedSizes.put(key, size);
////        return size;
//    }

//    private static boolean suggestedSizeFitsInSpace(TextView tv, CharSequence text, int suggestedSizeInPx, int right, int bottom) {
//        final int maxLines = getMaxLines(tv);
//        TextPaint mTempTextPaint = new TextPaint();
//        mTempTextPaint.set(tv.getPaint());
//        mTempTextPaint.setTextSize(suggestedSizeInPx);
//
////        StaticLayout (CharSequence source,
////                TextPaint paint,
////        int width,
////        Layout.Alignment align,
////        float spacingmult,
////        float spacingadd,
////        boolean includepad)
//
//        final StaticLayout.Builder layoutBuilder = StaticLayout.Builder.obtain(
//                text, 0, text.length(),  mTempTextPaint, Math.round(right));
//        layoutBuilder.setAlignment(tv.getLayoutAlignment())
//                .setLineSpacing(tv.getLineSpacingExtra(), tv.getLineSpacingMultiplier())
//                .setIncludePad(tv.getIncludeFontPadding())
//                .setUseLineSpacingFromFallbacks(tv.mUseFallbackLineSpacing)
//                .setBreakStrategy(tv.getBreakStrategy())
//                .setHyphenationFrequency(tv.getHyphenationFrequency())
//                .setJustificationMode(tv.getJustificationMode())
//                .setMaxLines(mMaxMode == LINES ? mMaximum : Integer.MAX_VALUE)
//                .setTextDirection(tv.getTextDirectionHeuristic());
//        final StaticLayout layout = layoutBuilder.build();
//        // Lines overflow.
//        if (maxLines != -1 && layout.getLineCount() > maxLines) {
//            return false;
//        }
//        // Height overflow.
//        if (layout.getHeight() > bottom) {
//            return false;
//        }
//        return true;
//    }

}