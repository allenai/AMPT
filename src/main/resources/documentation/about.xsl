<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        version="1.0"
>

    <xsl:output method="html"/>
    <xsl:template match="/">

        <!--
          ~  Copyright (c) 2021 The Allen Institute for Artificial Intelligence.
          ~
          ~ Licensed under the Apache License, Version 2.0 (the "License");
          ~ you may not use this file except in compliance with the License.
          ~ You may obtain a copy of the License at
          ~
          ~     http://www.apache.org/licenses/LICENSE-2.0
          ~
          ~ Unless required by applicable law or agreed to in writing, software
          ~ distributed under the License is distributed on an "AS IS" BASIS,
          ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
          ~ See the License for the specific language governing permissions and
          ~ limitations under the License.
          -->

        <html lang="en" xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <meta charset="UTF-8"/>
                <title>Aquatic Mammal Photogrammetry Tool</title>
            </head>
            <body>
                <h1>Aquatic Mammal Photogrammetry Tool</h1>
                <h2>Version: ${version}</h2>
                <p>Copyright 2021 The Allen Institute for Artificial Intelligence.</p>

                <p>Licensed under the Apache License, Version 2.0 (the "License");
                    you may not use this file except in compliance with the License.
                    You may obtain a copy of the License at
                </p>

                <p>
                    <a target="_blank" href="http://www.apache.org/licenses/LICENSE-2.0">
                        http://www.apache.org/licenses/LICENSE-2.0
                    </a>
                </p>

                <p>Unless required by applicable law or agreed to in writing, software
                    distributed under the License is distributed on an "AS IS" BASIS,
                    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                    See the License for the specific language governing permissions and
                    limitations under the License.
                </p>

                <h2>Dependencies</h2>
                <ul>
                    <xsl:for-each select="/attributionReport/dependencies/dependency">
                        <li>
                            <p>
                                <a target="_blank">
                                    <xsl:attribute name="href">
                                        <xsl:value-of select="projectUrl"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="name"/>
                                </a>
                                version
                                <xsl:value-of select="version"/>
                            </p>
                            <ul>
                                <xsl:for-each select="licenses/license">
                                    <li>
                                        <a target="_blank">
                                            <xsl:attribute name="href">
                                                <xsl:value-of select="url"/>
                                            </xsl:attribute>
                                            <xsl:value-of select="name"/>
                                        </a>
                                    </li>
                                </xsl:for-each>
                            </ul>
                        </li>
                    </xsl:for-each>
                </ul>

                <p>The development of the Aquatic Mammal Photogrammetry Tool was supported by <a target="_blank"
                                                                                                 href="https://allenai.org/">
                    The Allen Institute for Artificial Intelligence
                </a> and
                    <a target="_blank" href="https://www.vulcan.com/">Vulcan Inc.</a>
                </p>

                <h2>Special Thanks</h2>
                <ul>
                    <li>
                        <a target="_blank" href="https://pgafamilyfoundation.org/">Paul G. Allen Family Foundation</a>
                    </li>
                    <li>
                        <a target="_blank" href="https://www.sealifer3.org/">SR<sup>3</sup> SeaLife Response,
                            Rehabilitation, and
                            Research
                        </a>
                    </li>
                </ul>

            </body>
        </html>

    </xsl:template>
</xsl:stylesheet>
