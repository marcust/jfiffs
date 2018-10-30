package org.thiesen.jfiffs.storage.mapper;

import org.thiesen.jfiffs.storage.business.Result;
import org.thiesen.jfiffs.storage.proto.GetReply;
import org.thiesen.jfiffs.storage.proto.Reply;
import org.thiesen.jfiffs.storage.proto.Status;

public class ReplyMapper {
    public Reply toReply(Result result) {
        final Reply.Builder builder = Reply.newBuilder();

        switch (result) {
            case OK: builder.setStatus(Status.OK); break;
            case ERROR: builder.setStatus(Status.ERROR); break;
        }

        return builder.build();
    }

    public GetReply toGetReply(Double aDouble) {
        return GetReply.newBuilder()
                .setStatus(Status.OK)
                .setSimilarity(aDouble)
                .build();
    }
}
