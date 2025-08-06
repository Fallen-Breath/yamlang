package me.fallenbreath.yamlang;

import java.util.Collections;
import me.fallenbreath.yamlang.utils.StringUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;

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

				TaskProvider<YamlangConvertResourcesTask> provider = project.getTasks().register(taskName, YamlangConvertResourcesTask.class, task -> {
					task.dependsOn(sourceSet.getProcessResourcesTaskName());
					task.getOutputs().upToDateWhen(t -> false);

					DirectoryProperty destination = project.getObjects().directoryProperty().fileValue(sourceSet.getOutput().getResourcesDir());
					Provider<Directory> inputDir = destination.dir(extension.getInputDir().orElse(""));
					Provider<Directory> outputDir = destination.dir(extension.getOutputDir().orElse(extension.getInputDir().orElse("")));

					task.getInputDirectory().set(inputDir);
					task.getOutputDirectory().set(outputDir);
					task.getTargetFilePattern().set(extension.getTargetFilePattern());
					task.getPreserveYaml().set(extension.getPreserveYaml());
					task.getCharset().set(extension.getCharset());
					task.getOwolibRichTranslations().set(extension.getOwolibRichTranslations());
				});
				project.getTasks().getByName(sourceSet.getProcessResourcesTaskName(), task -> task.finalizedBy(provider));
				project.getTasks().getByName(sourceSet.getClassesTaskName(), task -> task.dependsOn(provider));
			}
		});
	}
}