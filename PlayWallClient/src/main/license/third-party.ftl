<#list dependencyMap as e>
    <#assign project = e.getKey()/>
    <#assign licenses = e.getValue()/>
    ${project.groupId}.${project.artifactId}|${project.version}|<#if licenses?has_content><#list licenses as license>${license}<#sep>, </#list><#else>Unknown</#if>
</#list>
