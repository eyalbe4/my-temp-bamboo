package org.jfrog.bamboo.api;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;

/**
 * Created by DimaN on 21/06/2016.
 */
public class MyFirstTask implements TaskType {

//		@Override
		public TaskResult execute(final TaskContext taskContext) throws TaskException
		{
			final BuildLogger buildLogger = taskContext.getBuildLogger();

			buildLogger.addBuildLogEntry("Hello, World!");

			return TaskResultBuilder.create(taskContext).success().build();
		}

}
