package com.ceyoniq.gradle.buildprocess;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class BuildprocessPlugin implements Plugin< Project > {

    @Override
    public void apply( Project project ) {
        
        // version extension
        project.getExtensions().add( VerInfo.EXT_KEY, VerInfo.from( project ) );

        // uploadBom task
        project.getTasks().register( "uploadBom", UploadBomTask.class );
    }
}
