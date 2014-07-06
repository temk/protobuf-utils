/**
   Copyright Katros LTD (2014)
**/
package net.katros.services.proto.grep.expr;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor;

/**
 *
 * @author boris@temk.org
 */
public class NoFieldException extends  RuntimeException {
     public NoFieldException(FieldDescriptor fd) {
         super("No such field: " + fd.getFullName());
     }
}
