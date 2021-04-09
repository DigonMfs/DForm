<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : teststylesheet.xsl
    Created on : 28 oktober 2012, 9:53
    Author     : rombouts
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>

    <!-- TODO customize transformation rules 
         syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template match="/">
        <table>
            <th>
                <td type="N">Patient ID</td>
                <td type="S">Pediatrician Name</td>
            </th>
            <tr>
                <td >5.5</td>
                <td >test</td>
                <td>2012-09-09</td>    
            </tr>
            
            <xsl:for-each select="DigonDFormOutput/subject">
                <tr>
                    <td>
                        <xsl:value-of select="@id"/>
                    </td>
                    <td>
                        <xsl:value-of select="DigonDocumentData/KeyValue[@Key='pediatrician_name']/@Value"/>
                    </td>
                                        <td>
                        <xsl:value-of select="DigonDocumentData/KeyValue[@Key='birth_date']/@Value"/>
                    </td>

                </tr>
            </xsl:for-each>

            
        </table>
    </xsl:template>

</xsl:stylesheet>
