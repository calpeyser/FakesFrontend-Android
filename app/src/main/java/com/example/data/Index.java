package com.example.data;

import android.util.Pair;
import android.widget.SpinnerAdapter;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.Immutable;

public class Index {

    public static class Fake {

        @SerializedName("display_name")
        private String displayName;

        @SerializedName("file_name")
        private String fileName;

        public String getDisplayName() {
            return displayName;
        }

        public String getFileName() {
            return fileName;
        }
    }

    public static class Book {

        @SerializedName("book_name")
        private String bookName;

        @SerializedName("book_code")
        private String bookCode;

        @SerializedName("key")
        private String key;

        @SerializedName("fakes")
        private List<Fake> fakes;

        public String getBookName() {
            return bookName;
        }

        public String getBookCode() {
            return bookCode;
        }

        public String getKey() {
            return key;
        }

        public List<Fake> getFakes() {
            return fakes;
        }
    }

    public static final Index EMPTY = new Index(ImmutableList.<Book>of());

    private List<Book> books;

    public Index(List<Book> books) {
        this.books = books;
    }

    public List<Book> getBooks() {
        return books;
    }

    public ImmutableList<String> getFakeDisplayNames() {
        ImmutableList.Builder<String> result = ImmutableList.builder();
        for (Book book : books) {
            for (Fake fake : book.getFakes()) {
                result.add(fake.getDisplayName());
            }
        }
        return result.build();
    }

    public List<String> getKeysForFake(String fakeDisplayName) {
        ArrayList<String> result = new ArrayList<>();
        for (Book book : books) {
            for (Fake fake : book.getFakes()) {
                if (fake.getDisplayName().equals(fakeDisplayName)) {
                    result.add(book.key);
                }
            }
        }
        return result;
    }

    public Pair<Book, Fake> getBookAndFakeForNameAndKey(String fakeDisplayName, String key) {
        for (Book book : books) {
            if (book.key.equals(key)) {
                for (Fake fake : book.getFakes()) {
                    if (fake.getDisplayName().equals(fakeDisplayName)) {
                        return Pair.create(book, fake);
                    }
                }
            }
        }
        throw new IllegalArgumentException("Book for fake with name " + fakeDisplayName + " and key " + key + " not found.");
    }

    public int getSize() {
        int res = 0;
        for (Book book : books) {
            for (Fake fake : book.getFakes()) {
                res++;
            }
        }
        return res;
    }
}
