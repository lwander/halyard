/*
 * Copyright 2017 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.halyard.deploy.job.v1;

import com.netflix.spinnaker.halyard.deploy.job.v1.JobStatus.State;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

//TODO(lwander) unify with original job executor: https://github.com/spinnaker/rosco/blob/bf718907888a7d95a0da6e21ec0e00c0709c4e19/rosco-core/src/main/groovy/com/netflix/spinnaker/rosco/jobs/JobExecutor.groovy
public abstract class JobExecutor {
  abstract public String startJob(JobRequest jobRequest, Map<String, String> env, InputStream stdIn);

  abstract public boolean jobExists(String jobId);

  abstract public JobStatus updateJob(String jobId);

  abstract public void cancelJob(String jobId);

  public String startJob(JobRequest jobRequest) {
    return startJob(jobRequest, System.getenv(), new ByteArrayInputStream("".getBytes()));
  }

  public JobStatus backoffWait(String jobId, long minWaitMillis, long maxWaitMillis) {
    JobStatus result = updateJob(jobId);
    long waitTime = minWaitMillis;
    while (result == null || result.getState() == State.RUNNING) {
      try {
        Thread.sleep(waitTime);
      } catch (InterruptedException ignored) {
      }

      waitTime <<= 1;
      waitTime = Math.min(maxWaitMillis, waitTime);
      result = updateJob(jobId);
    }

    return result;
  }
}