package com.eastcom.common.utils.split;

import java.util.Arrays;

/**
 * Created by slp on 2016/3/16.
 */
public class StringSplitHelp {
    private int length;
    private int[] needColumnIndex;
    private int[] sortIndex;
    private char separator;

    public StringSplitHelp(int[] needColumnIndex, char separator) {
        this.needColumnIndex = needColumnIndex;
        this.length = needColumnIndex.length;
        this.separator = separator;
        sortIndex = new int[length];
        int[] temp = Arrays.copyOf(needColumnIndex, length);
        int maxIndex = -1;
        int max = -1;
        for (int i = 1; i <= length; i++) {
            for (int j = 0; j < length; j++) {
                if (temp[j] != -1 && max < temp[j]) {
                    max = temp[j];
                    maxIndex = j;
                }
            }
            sortIndex[length - i] = maxIndex;
            temp[maxIndex] = -1;
            max = -1;
        }
    }

    public String[] findNeededColumn(byte[] str, int offset, int length) {
        int maxLength = offset + length;
        if (str == null || str.length < maxLength || offset < 0 || length < 0) {
            return null;
        }

        String[] result = new String[this.length];
        int index = 0;
        int c = needColumnIndex[sortIndex[index]];
        int count = 0;
        int n = 0;
        int off = offset;
        int next = 0;
        while ((next = indexOf(str, separator, off, maxLength)) != -1) {
            if (count == c) {
                result[sortIndex[index]] = new String(str, off, next - off);
                n++;
                index++;
                if (index < this.length) {
                    c = needColumnIndex[sortIndex[index]];
                } else {
                    break;
                }
            }
            count++;
            off = next + 1;
        }
        if (n != this.length) {
            return null;
        }
        return result;
    }

    private int indexOf(byte[] str, int ch, int fromIndex, int max) {
        for (int i = fromIndex; i < max; i++) {
            if (str[i] == ch) {
                return i;
            }
        }
        return -1;
    }
}
