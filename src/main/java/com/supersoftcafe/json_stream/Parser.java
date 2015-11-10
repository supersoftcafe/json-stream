package com.supersoftcafe.json_stream;


import com.supersoftcafe.json_stream.impl.ParserImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public interface Parser {
    CallbackParser callbackParser();

    <T> Iterator<T> iterator(InputStream in, TypeRef<T> type, String... paths);
    <T> Iterator<T> iterator(Reader      in, TypeRef<T> type, String... paths);

    <T> Optional<T> getFirst(InputStream in, TypeRef<T> type, String... paths) throws IOException;
    <T> Optional<T> getFirst(Reader      in, TypeRef<T> type, String... paths) throws IOException;


    default <T> Iterator<T> iterator(InputStream in, Class<T> type, String... paths) {
        return iterator(in, TypeRef.of(type), paths);}
    default <T> Iterator<T> iterator(Reader      in, Class<T> type, String... paths) {
        return iterator(in, TypeRef.of(type), paths);}


    default <T> Optional<T> getFirst(InputStream in, Class<T> type, String... paths) throws IOException {
        return getFirst(in, TypeRef.of(type), paths);}
    default <T> Optional<T> getFirst(Reader in, Class<T> type, String... paths) throws IOException {
        return getFirst(in, TypeRef.of(type), paths);}


    default <T> T getOne(InputStream in, Class<T> type, String... paths) throws IOException {
        return getFirst(in, type, paths).get();}
    default <T> T getOne(InputStream in, TypeRef<T> type, String... paths) throws IOException {
        return getFirst(in, type, paths).get();}
    default <T> T getOne(Reader in, Class<T> type, String... paths) throws IOException {
        return getFirst(in, type, paths).get();}
    default <T> T getOne(Reader in, TypeRef<T> type, String... paths) throws IOException {
        return getFirst(in, type, paths).get();}


    default <T> Stream<T> stream(InputStream in, Class<T> type, String... paths) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(in, type, paths), 0), false);}
    default <T> Stream<T> stream(InputStream in, TypeRef<T> type, String... paths) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(in, type, paths), 0), false);}
    default <T> Stream<T> stream(Reader in, Class<T> type, String... paths) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(in, type, paths), 0), false);}
    default <T> Stream<T> stream(Reader in, TypeRef<T> type, String... paths) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(in, type, paths), 0), false);}
}
