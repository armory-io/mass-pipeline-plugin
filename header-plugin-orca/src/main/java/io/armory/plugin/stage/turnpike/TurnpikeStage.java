package io.armory.plugin.stage.turnpike;

import com.google.common.base.Strings;
import com.netflix.spinnaker.kork.exceptions.ConfigurationException;
import com.netflix.spinnaker.orca.api.pipeline.SyntheticStageOwner;
import com.netflix.spinnaker.orca.api.pipeline.graph.StageDefinitionBuilder;
import com.netflix.spinnaker.orca.api.pipeline.graph.StageGraphBuilder;
import com.netflix.spinnaker.orca.api.pipeline.graph.TaskNode;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

import com.netflix.spinnaker.orca.front50.pipeline.PipelineStage;
import com.netflix.spinnaker.orca.front50.tasks.MonitorPipelineTask;
import com.netflix.spinnaker.orca.front50.tasks.StartPipelineTask;
import com.netflix.spinnaker.orca.pipeline.StageExecutionFactory;
import com.netflix.spinnaker.orca.pipeline.model.StageExecutionImpl;
import com.netflix.spinnaker.orca.pipeline.tasks.artifacts.BindProducedArtifactsTask;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class TurnpikeStage implements StageDefinitionBuilder {

  @Override
  public void beforeStages(@NotNull StageExecution parent, @NotNull StageGraphBuilder graph) {
    // trigger all the downstream pipelines

    String inputName = (String) parent.getContext().getOrDefault("inputKey", "input");
    Set<String> inputs = (HashSet<String>) parent.getContext().getOrDefault("inputs", new HashSet<>());
    String pipeToTrigger = (String) parent.getContext().get("pipelineToTrigger"); // from context

    // If there's no pipeline to trigger then there's no useful work to do.
    if (Strings.isNullOrEmpty(pipeToTrigger)) {
      throw new ConfigurationException("TODO ERROR MESSAGE");
    }

    for (String input : inputs ) {
      Map<String, Object> context = new HashMap<>();
      Map<String, Object> parameters = new HashMap<>();

      // This is the car VIN that we want to trigger downstream pipelines with.
      parameters.put(inputName, input);

      // TODO: this will be sad if application isn't set
      context.put("application", parent.getContext().get("application"));
      // TODO: this will be sad if strategyApplication isn't set
      context.put("pipelineApplication", parent.getContext().get("strategyApplication"));
      context.put("pipelineId", pipeToTrigger);
      context.put("pipelineParameters", parameters);

      StageExecution sei = StageExecutionFactory.newStage(
              parent.getExecution(),
              PipelineStage.PIPELINE_CONFIG_TYPE,
              "pipeline",
              context,
              parent,
              SyntheticStageOwner.STAGE_BEFORE
      );

      graph.add(sei);
    }

  }

  @Override
  public void taskGraph(@Nonnull StageExecution stage, @Nonnull TaskNode.Builder builder) {
    // monitor all the pipelines

  }
}
