package me.fallenbreath.yamlang;

import me.fallenbreath.yamlang.utils.StringUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.SourceSet;

import java.util.Collections;

public class YamlangPlugin implements Plugin<Project>
{
	@Override
	public void apply(Project project)
	{
		YamlangExtension extension = project.getExtensions().create("yamlang", YamlangExtension.class);
		project.afterEvaluate(p -> {
			for (SourceSet sourceSet : extension.getTargetSourceSets().getOrElse(Collections.emptyList()))
			{
				String taskName = String.format("yamlangConvert%sResources", StringUtils.capitalize(sourceSet.getName()));
				project.getLogger().info("Hooking sourceset {}", sourceSet);
				Task processResources = project.getTasks().getByName(sourceSet.getProcessResourcesTaskName());

				project.getTasks().register(taskName, YamlangConvertor.class, task -> {
					task.setSourceSet(sourceSet);
					task.onlyIf(s -> !processResources.getState().getUpToDate());
				});
				processResources.finalizedBy(taskName);
			}
		});
	}
}