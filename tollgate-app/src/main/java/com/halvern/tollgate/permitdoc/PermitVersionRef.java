package com.halvern.tollgate.permitdoc;

/**
 * One version of a permit, written {@code HX-P-2023-00042/2}.
 *
 * <p>{@link PermitReference} identifies the permit a haulier quotes and keeps across amendments; this identifies
 * the printed document and the status history of a single version, which is what the checkpoint staff see.
 */
public record PermitVersionRef(PermitReference permit, int version) {

    public PermitVersionRef {
        if (permit == null || version < 1) {
            throw new IllegalArgumentException("A permit version needs a permit reference and a version from 1");
        }
    }

    public static PermitVersionRef of(String permitReference, int version) {
        return new PermitVersionRef(PermitReference.parse(permitReference), version);
    }

    public static PermitVersionRef parse(String text) {
        int slash = text == null ? -1 : text.lastIndexOf('/');
        if (slash < 0) {
            throw new IllegalArgumentException("Not a permit version reference: " + text);
        }
        return of(text.substring(0, slash), Integer.parseInt(text.substring(slash + 1)));
    }

    /** Safe for a file name: {@code HX-P-2023-00042-v2}. */
    public String fileName() {
        return permit + "-v" + version;
    }

    @Override
    public String toString() {
        return permit + "/" + version;
    }
}
