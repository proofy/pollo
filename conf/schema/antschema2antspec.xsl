<?xml version="1.0" encoding="ISO-8859-1"?>
<!--
	This stylesheets automatically generates the file antspec.xml based on
	the file antschema.xml
-->
<xsl:stylesheet	version="1.0"
				xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                >

<xsl:variable name="tasks" select="/schema/element[@name='target']/allowedsubelements/@names"/>

<xsl:template match="schema">
	<displayspec>

		<xsl:comment>
			This file was automatically generated by antschema2antspec.xsl.
		</xsl:comment>

		<xsl:apply-templates/>
	</displayspec>
</xsl:template>

<xsl:template match="element">
	<element name="{@name}">
		<xsl:choose>
			<xsl:when test="@name='project'">
        <xsl:attribute name="background-color">e0eee0</xsl:attribute>
			</xsl:when>
			<xsl:when test="@name='target'">
        <xsl:attribute name="background-color">ff9d70</xsl:attribute>
			</xsl:when>
			<!-- if it is a deprecated task -->
			<xsl:when test="@name='copydir' or @name='copyfile' or @name='rename' or @name='javadoc2' or @name='execon' or @name='deltree'">
        <xsl:attribute name="background-color">ff6026</xsl:attribute>
			</xsl:when>
			<!-- if it is a task (why does ends-with() not exist?)-->
			<xsl:when test="contains($tasks, concat(',',@name,',')) or contains($tasks, concat(@name,','))">
        <xsl:attribute name="background-color">8aeada</xsl:attribute>
			</xsl:when>
			<!-- otherwise -->
			<xsl:otherwise>
        <xsl:attribute name="background-color">e2d0d0</xsl:attribute>
			</xsl:otherwise>
		</xsl:choose>
    <attributes>
      <xsl:for-each select="attributes/attribute">
        <attribute name="{@name}"/>
      </xsl:for-each>
    </attributes>
	</element>
</xsl:template>

</xsl:stylesheet>
