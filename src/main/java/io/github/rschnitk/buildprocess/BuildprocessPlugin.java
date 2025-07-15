package io.github.rschnitk.buildprocess;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.plugins.PluginAware;

/**
 * Buildprocess plug-in: add extension "verInfo"
 */
public class BuildprocessPlugin implements Plugin<PluginAware> {

    @Override
    public void apply( PluginAware pluginAware ) {

        // version extension
        if ( pluginAware instanceof Project ) {
            Project project = (Project) pluginAware;
            project.getExtensions().add( VerInfo.EXT_KEY, VerInfo.from( project ) );
        }
        if ( pluginAware instanceof Settings ) {
            Settings settings = (Settings) pluginAware;
            VerInfo verInfo = VerInfo.from( settings );
            settings.getExtensions().getExtraProperties().set(VerInfo.EXT_KEY, verInfo);
            settings.getExtensions().getExtraProperties().set("fullVersion", verInfo.getFullVersion() );
            settings.getExtensions().add( VerInfo.EXT_KEY, verInfo );
        }
    }
}
