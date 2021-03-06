// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: array.proto

package ru.hse.utils.protocols;

public interface ArrayOrBuilder extends
    // @@protoc_insertion_point(interface_extends:utils.Array)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>required int32 id = 1;</code>
   * @return Whether the id field is set.
   */
  boolean hasId();
  /**
   * <code>required int32 id = 1;</code>
   * @return The id.
   */
  int getId();

  /**
   * <code>repeated int32 array = 2;</code>
   * @return A list containing the array.
   */
  java.util.List<java.lang.Integer> getArrayList();
  /**
   * <code>repeated int32 array = 2;</code>
   * @return The count of array.
   */
  int getArrayCount();
  /**
   * <code>repeated int32 array = 2;</code>
   * @param index The index of the element to return.
   * @return The array at the given index.
   */
  int getArray(int index);
}
