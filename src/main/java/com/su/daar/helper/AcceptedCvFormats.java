package com.su.daar.helper;

public enum AcceptedCvFormats {
    pdf,
    doc;

    @Override
    public String toString() {
        return "."+super.toString();
    }
}
