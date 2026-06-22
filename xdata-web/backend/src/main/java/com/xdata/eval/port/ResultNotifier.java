package com.xdata.eval.port;

import com.xdata.model.Submission;

/** Port for pushing the graded result to the student (WebSocket in prod). */
public interface ResultNotifier {

    void notifyGraded(Submission submission);
}
