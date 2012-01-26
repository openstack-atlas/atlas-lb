<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:vc="http://www.w3.org/2007/XMLSchema-versioning"
>

 <!--* 

    xsd.vc.xsl: XSLT 1.0 transform for XSD 1.1 conditional inclusion

    Copyright (C) 2009 Black Mesa Technologies LLC

    This program is free software: you can redistribute it and/or
    modify it under the terms of the GNU General Public License as
    published by the Free Software Foundation, either version 3 of
    the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public
    License along with this program, in the Licenses subdirectory,
    in file GNU_GPL.

    If the license was not provided, or is missing, see 
    <http://www.gnu.org/licenses/>.

 *-->

 <!--* 
 This stylesheet performs a conditiona inclusion filter as specified
 by XML Schema Definition Language (XSD) 1.1.

    http://www.w3.org/XML/Group/2004/06/xmlschema-1/structures.html#cip

 Elements are copied through to the output unless (a) they carry one
 of the known version-control attributes and (b) the test associated
 with that attribute values for that element.
 
 The stylesheet is parameterized with a version number, a list of
 available types, and a list of available facets.

 The version number vc.Version is a decimal number.  (This XSLT 1.0
 stylesheet will treat is as a double, but this will be an issue only in
 artificial cases (andpresumably some tests in the test suite.)
 Since this implementation is originally intended to illustrate 
 conditional inclusion for a 1.1 processor, it defaults to 1.1.
 *-->

 <xsl:param name="vc.Version" select="number(1.1)"/>

 <!--* 
 The lists of available types and available facets are expanded names
 (NOT QNames) in {namespace-uri}localname form.  For convenience,
 there are separate parameters for the built-in types and facets (which
 will never need to change) and additional ones.  This should make it
 easier to experiment with this stylesheet to see what effect it will
 have on a schema document if the {foo}bar type is automatically
 available.
 *-->
 
 <xsl:param name="vc.Built-InTypes">
  {http://www.w3.org/2001/XMLSchema}anyType
  {http://www.w3.org/2001/XMLSchema}anySimpleType
  {http://www.w3.org/2001/XMLSchema}anyAtomicType
  {http://www.w3.org/2001/XMLSchema}string
  {http://www.w3.org/2001/XMLSchema}boolean
  {http://www.w3.org/2001/XMLSchema}decimal
  {http://www.w3.org/2001/XMLSchema}precisionDecimal
  {http://www.w3.org/2001/XMLSchema}float
  {http://www.w3.org/2001/XMLSchema}double
  {http://www.w3.org/2001/XMLSchema}duration
  {http://www.w3.org/2001/XMLSchema}dateTime
  {http://www.w3.org/2001/XMLSchema}time
  {http://www.w3.org/2001/XMLSchema}date
  {http://www.w3.org/2001/XMLSchema}gYearMonth
  {http://www.w3.org/2001/XMLSchema}gYear
  {http://www.w3.org/2001/XMLSchema}gMonthDay
  {http://www.w3.org/2001/XMLSchema}gDay
  {http://www.w3.org/2001/XMLSchema}gMonth
  {http://www.w3.org/2001/XMLSchema}hexBinary
  {http://www.w3.org/2001/XMLSchema}base64Binary
  {http://www.w3.org/2001/XMLSchema}anyURI
  {http://www.w3.org/2001/XMLSchema}QName
  {http://www.w3.org/2001/XMLSchema}NOTATION
  {http://www.w3.org/2001/XMLSchema}normalizedString
  {http://www.w3.org/2001/XMLSchema}token
  {http://www.w3.org/2001/XMLSchema}language
  {http://www.w3.org/2001/XMLSchema}NMTOKEN
  {http://www.w3.org/2001/XMLSchema}NMTOKENS
  {http://www.w3.org/2001/XMLSchema}Name
  {http://www.w3.org/2001/XMLSchema}NCName
  {http://www.w3.org/2001/XMLSchema}ID
  {http://www.w3.org/2001/XMLSchema}IDREF
  {http://www.w3.org/2001/XMLSchema}IDREFS
  {http://www.w3.org/2001/XMLSchema}ENTITY
  {http://www.w3.org/2001/XMLSchema}ENTITIES
  {http://www.w3.org/2001/XMLSchema}integer
  {http://www.w3.org/2001/XMLSchema}nonPositiveInteger
  {http://www.w3.org/2001/XMLSchema}negativeInteger
  {http://www.w3.org/2001/XMLSchema}long
  {http://www.w3.org/2001/XMLSchema}int
  {http://www.w3.org/2001/XMLSchema}short
  {http://www.w3.org/2001/XMLSchema}byte
  {http://www.w3.org/2001/XMLSchema}nonNegativeInteger
  {http://www.w3.org/2001/XMLSchema}unsignedLong
  {http://www.w3.org/2001/XMLSchema}unsignedInt
  {http://www.w3.org/2001/XMLSchema}unsignedShort
  {http://www.w3.org/2001/XMLSchema}unsignedByte
  {http://www.w3.org/2001/XMLSchema}positiveInteger
  {http://www.w3.org/2001/XMLSchema}yearMonthDuration
  {http://www.w3.org/2001/XMLSchema}dayTimeDuration 
  {http://www.w3.org/2001/XMLSchema}error
 </xsl:param>
 <xsl:param name="vc.Built-InFacets">
  {http://www.w3.org/2001/XMLSchema}
  {http://www.w3.org/2001/XMLSchema}length 
  {http://www.w3.org/2001/XMLSchema}minLength 
  {http://www.w3.org/2001/XMLSchema}maxLength 
  {http://www.w3.org/2001/XMLSchema}pattern 
  {http://www.w3.org/2001/XMLSchema}enumeration 
  {http://www.w3.org/2001/XMLSchema}whiteSpace 
  {http://www.w3.org/2001/XMLSchema}maxInclusive 
  {http://www.w3.org/2001/XMLSchema}maxExclusive 
  {http://www.w3.org/2001/XMLSchema}minExclusive 
  {http://www.w3.org/2001/XMLSchema}minInclusive 
  {http://www.w3.org/2001/XMLSchema}totalDigits 
  {http://www.w3.org/2001/XMLSchema}fractionDigits 
  {http://www.w3.org/2001/XMLSchema}maxScale 
  {http://www.w3.org/2001/XMLSchema}minScale 
  {http://www.w3.org/2001/XMLSchema}assertion 
  {http://www.w3.org/2001/XMLSchema}explicitTimezone
 </xsl:param>
 <xsl:param name="vc.AdditionalTypes"/>
 <xsl:param name="vc.AdditionalFacets"/>

 <xsl:variable name="vc.AvailableTypes" select="concat(
  ' ',
  normalize-space($vc.Built-InTypes),
  ' ',
  normalize-space($vc.AdditionalTypes),
  ' ')"/>
 <xsl:variable name="vc.AvailableFacets" select="concat(
  ' ',
  normalize-space($vc.Built-InFacets),
  ' ',
  normalize-space($vc.AdditionalFacets),
  ' ')"/>

 <!--*
 The vc attribute supported and their meaning are:

 vc:minVersion succeeds if @vc:minVersion is less than or equal to
 $vc.Version.  (It's an inclusive test: equal values succeed.

 vc:maxVersion succeeds if @vc:maxVersion is greater than to
 $vc.Version.  It's an exclusive test: equality fails.)

 vc:typeAvailable succeeds if every type named is available.  (So
 vc:typeAvailable="a b c" means succeed if a AND b AND c are
 available.)

 vc:typeUnavailable succeeds if any type named is unavailable.  (So
 vc:typeUnavailable="a b c" means succeed if a OR b OR c is
 unavailable.)

 vc:facetAvailable and vc:facetUnavailable work like typeAvailable
 and typeUnavailable, but for facets not types.

 *-->

 <!--* misc housekeeping *-->
 <xsl:param name="vc.ns">http://www.w3.org/2007/XMLSchema-versioning</xsl:param>
 <xsl:variable name="vc.Names"> minVersion maxVersion typeAvailable 
  typeUnvailable facetAvailable facetUnavailable </xsl:variable>
 <xsl:variable name="message-level" select="1"/>
 <xsl:variable name="debug-keywords">len.from.lqn check-avail</xsl:variable>
 <!--* check-avail sub-set-check len-from-lqn *-->

 <!--* If an element has a vc:* attribute, do the right thing *-->
 <xsl:template match="*[@vc:*]">
  <xsl:choose>
   <xsl:when test="@vc:minVersion and ($vc.Version &lt; @vc:minVersion)"/>
   <xsl:when test="@vc:maxVersion and not($vc.Version &lt; @vc:maxVersion)"/>
   <xsl:when test="count(@vc:typeAvailable 
    | @vc:typeUnavailable
    | @vc:facetAvailable 
    | @vc:facetUnavailable) > 0">
    <!--* check type and facet availability *-->
    <xsl:variable name="flags">
     <xsl:if test="@vc:typeAvailable">
      <xsl:call-template name="check-available-qn-en-kw">
       <xsl:with-param name="probes" select="@vc:typeAvailable"/>
       <xsl:with-param name="available" select="$vc.AvailableTypes"/>
       <xsl:with-param name="polarity">subset-go</xsl:with-param>
      </xsl:call-template>
     </xsl:if>
     <xsl:if test="@vc:typeUnavailable">
      <xsl:call-template name="check-available-qn-en-kw">
       <xsl:with-param name="probes" select="@vc:typeUnavailable"/>
       <xsl:with-param name="available" select="$vc.AvailableTypes"/>
       <xsl:with-param name="polarity">subset-nogo</xsl:with-param>
      </xsl:call-template>
     </xsl:if>
     <xsl:if test="@vc:facetAvailable">
      <xsl:call-template name="check-available-qn-en-kw">
       <xsl:with-param name="probes" select="@vc:facetAvailable"/>
       <xsl:with-param name="available" select="$vc.AvailableFacets"/>
       <xsl:with-param name="polarity">subset-go</xsl:with-param>
      </xsl:call-template>
     </xsl:if>
     <xsl:if test="@vc:facetUnavailable">
      <xsl:call-template name="check-available-qn-en-kw">
       <xsl:with-param name="probes" select="@vc:facetUnavailable"/>
       <xsl:with-param name="available" select="$vc.AvailableFacets"/>
       <xsl:with-param name="polarity">subset-nogo</xsl:with-param>
      </xsl:call-template>
     </xsl:if>
    </xsl:variable>
    <xsl:if test="$message-level > 3 and contains($debug-keywords,'check-avail')">
     <xsl:message>type available "<xsl:value-of select="@vc:typeAvailable"/>"</xsl:message>
     <xsl:message>type unavailable "<xsl:value-of select="@vc:typeUnavailable"/>"</xsl:message>
     <xsl:message>facet available "<xsl:value-of select="@vc:facetAvailable"/>"</xsl:message>
     <xsl:message>facet unavailable "<xsl:value-of select="@vc:facetUnavailable"/>"</xsl:message>
     <xsl:message>flags = "<xsl:value-of select="$flags"/>"</xsl:message>
    </xsl:if>
    <xsl:choose>
     <xsl:when test="contains($flags,'nogo')"></xsl:when>
     <xsl:otherwise>
      <xsl:call-template name="identity"/>
     </xsl:otherwise>
    </xsl:choose>
   </xsl:when>
   <xsl:otherwise>
    <!--* no known vc:* attributes *-->
    <xsl:variable name="vc.unknowns" select="./attribute::vc:*[namespace-uri() = $vc.ns 
     and not(contains($vc.Names,local-name()))]"/>
    <xsl:if test="count($vc.unknowns) > 0">
     <xsl:message>xsd.vc.xsl:  Unknown vc:* attribute: <xsl:value-of
       select="local-name($vc.unknowns)"/>, ignoring it ...</xsl:message>
    </xsl:if>
    <xsl:call-template name="identity"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>

 <xsl:template name="check-available-qn-en-kw">
  <xsl:param name="probes"/>
  <xsl:param name="available"/>
  <xsl:param name="polarity">subset-go</xsl:param>

  <xsl:variable name="lenNeedles">
   <xsl:call-template name="len-from-lqn">
    <xsl:with-param name="lqn" select="$probes"/>
   </xsl:call-template>
  </xsl:variable>
  <xsl:if test="$message-level > 4 and contains($debug-keywords,'len.from.lqn')">
   <xsl:message>check-available-qn-en-kw:  lqn of needles = "<xsl:value-of select="$probes"/>"</xsl:message>
   <xsl:message>check-available-qn-en-kw:  len of needles = "<xsl:value-of select="$lenNeedles"/>"</xsl:message>
  </xsl:if>
  <xsl:variable name="is-subset">
   <xsl:call-template name="subset-check">
    <xsl:with-param name="needles" select="$lenNeedles"/>
    <xsl:with-param name="haystack" select="$available"/>
   </xsl:call-template>
  </xsl:variable>
  <xsl:choose>
   <xsl:when test="$is-subset = 'true'
    and $polarity = 'subset-go'">go</xsl:when>
   <xsl:when test="$is-subset = 'true'
    and $polarity = 'subset-nogo'">nogo</xsl:when>
   <xsl:when test="$is-subset = 'false'
    and $polarity = 'subset-go'">nogo</xsl:when>
   <xsl:when test="$is-subset = 'false'
    and $polarity = 'subset-nogo'">go</xsl:when>
   <xsl:otherwise>
    <xsl:message terminate="yes">check-available-eqn-en-kw: confused.</xsl:message>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>

 <!--* len-from-lqn:  make list of expanded names from
     * list of QNames *-->
 <xsl:template name="len-from-lqn">
  <!--* lqn:  input to be processed.  len:  accumulated output *-->
  <xsl:param name="lqn"/>
  <xsl:param name="len"/>
  <xsl:call-template name="len-from-lqn-fragile">
   <xsl:with-param name="lqn" select="concat(normalize-space($lqn),' ')"/>
   <xsl:with-param name="len" select="normalize-space($len)"/>
  </xsl:call-template>
 </xsl:template>

 <!--* len-from-lqn-fragile:  assume whitespace normalization.
     *  *-->
 <xsl:template name="len-from-lqn-fragile">
  <!--* lqn:  input to be processed.  len:  accumulated output *-->
  <xsl:param name="lqn"/>
  <xsl:param name="len"/>

  <xsl:if test="$message-level > 4 and contains($debug-keywords,'len-from-lqn')">
   <xsl:message>len-from-lqn: lqn="<xsl:value-of select="$lqn"/>"</xsl:message>
   <xsl:message>len-from-lqn: len="<xsl:value-of select="$len"/>"</xsl:message>
  </xsl:if>
  <!--* break off one qname, make an expanded name, recur *-->
  <xsl:choose>
   <xsl:when test="$lqn = '' or $lqn = ' '">
    <!--* if lqn is empty, we are done.  Return what we got. *-->
    <xsl:if test="$message-level > 4 and contains($debug-keywords,'len-from-lqn')">
     <xsl:message>len-from-lqn returning: "<xsl:value-of select="$len"/>"</xsl:message>
    </xsl:if>
    <xsl:value-of select="$len"/>
   </xsl:when>
   <xsl:otherwise>

    <!--* break off one QName *-->
    <xsl:variable name="qnCur" select="substring-before($lqn,' ')"/>
    <xsl:variable name="lqnRest" select="substring-after($lqn,' ')"/>

    <!--* make an expanded name *-->
    <xsl:variable name="nsCur">
     <xsl:call-template name="qname.uri">
      <xsl:with-param name="qname" select="$qnCur"/>
     </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="lnameCur">
     <xsl:call-template name="qname.ncname">
      <xsl:with-param name="qname" select="$qnCur"/>
     </xsl:call-template>
    </xsl:variable>

    <!--* ... and recur *-->
    <xsl:call-template name="len-from-lqn">
     <xsl:with-param name="lqn" select="$lqnRest"/>
     <xsl:with-param name="len" select="concat($len,' {',$nsCur,'}',$lnameCur,' ')"/>
    </xsl:call-template>
   </xsl:otherwise>
  </xsl:choose>

 </xsl:template>

 <xsl:template name="qname.uri" match="*" mode="qname.uri">
  <xsl:param name="qname" select="string(.)"/>
  
  <xsl:variable name="prefix" select="substring-before($qname,':')"/>

  <xsl:if test="$message-level > 4 and contains($debug-keywords,'len-from-lqn')">
   <xsl:message>qname.uri entered with "<xsl:value-of select="$qname"/>", prefix is "<xsl:value-of select="$prefix"/>"</xsl:message>
  </xsl:if>

  <xsl:choose>
   <xsl:when test="(1=1) and ($prefix='xml')">
    <!--* we need to special-case 'xml', since
        * Opera does not provide a ns node for it.
        *-->
    <xsl:value-of select="'http://www.w3.org/XML/1998/namespace'"/>
   </xsl:when>
   <xsl:when test="self::*">
    <!--* we're an element *-->
    <xsl:value-of select="string(namespace::*[name()=$prefix])"/>
   </xsl:when>
   <xsl:otherwise>
    <!--* we're not an element *-->
    <xsl:value-of select="string(parent::*/namespace::*[name()=$prefix])"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>
 <xsl:template name="qname.ncname">
  <xsl:param name="qname" select="."/>
  <xsl:choose>
   <xsl:when test="contains($qname,':')">
    <xsl:value-of select="substring-after($qname,':')"/>
   </xsl:when>
   <xsl:otherwise>
    <xsl:value-of select="$qname"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>


 <!--* subset-check: given space-delimited strings $needles and
     * $haystack, check whether each token in $needles 
     * occurs as a token in $haystack or not.
     *-->
 <xsl:template name="subset-check">
  <xsl:param name="needles"/>
  <xsl:param name="haystack"/>
  <xsl:if test="$message-level > 3 and contains($debug-keywords,'sub-set-check')">
   <xsl:message>subset-check <xsl:value-of 
     select="concat('n=&quot;',$needles,'&quot; h=&quot;',$haystack,'&quot;')"/>
   </xsl:message>
  </xsl:if>
  <xsl:call-template name="subset-check-fragile">
   <xsl:with-param name="needles" select="concat(normalize-space($needles),' ')"/>
   <xsl:with-param name="haystack" select="concat(' ',normalize-space($haystack),' ')"/>
  </xsl:call-template>
 </xsl:template>

  <!--* subset-check-fragile assumes strings are already normalized *-->
 <xsl:template name="subset-check-fragile">
  <xsl:param name="needles"/>
  <xsl:param name="haystack"/>

  <xsl:if test="$message-level > 4 and contains($debug-keywords,'sub-set-check')">
   <xsl:message>subset-check-fragile <xsl:value-of 
     select="concat('n=&quot;',$needles,'&quot; h=&quot;',$haystack,'&quot;')"/>
    </xsl:message>
  </xsl:if>

  <!--* break off one token, test, return or recur *-->
  <xsl:choose>
   <xsl:when test="$needles = '' or $needles=' '">
    <!--* if needles is empty, we are done.  Return true. *-->
    <xsl:value-of select="'true'"/>
   </xsl:when>
   <xsl:otherwise>

    <!--* break off one token *-->
    <xsl:variable name="tokCur" select="substring-before($needles,' ')"/>
    <xsl:variable name="needlesRest" select="substring-after($needles,' ')"/>

    <!--* test and return or recur *-->
    <xsl:choose>
     <xsl:when test="contains($haystack,concat(' ',$tokCur,' '))">
      <xsl:call-template name="subset-check-fragile">
       <xsl:with-param name="needles" select="$needlesRest"/>
       <xsl:with-param name="haystack" select="$haystack"/>
      </xsl:call-template>
     </xsl:when>
     <xsl:otherwise>
      <xsl:value-of select="'false'"/>
     </xsl:otherwise>
    </xsl:choose>
   </xsl:otherwise>
  </xsl:choose>

 </xsl:template>

<!--* Otherwise, perform an identity transform *-->

 <xsl:template match='comment()'>
  <xsl:comment>
   <xsl:value-of select="."/>
  </xsl:comment>
 </xsl:template>
 
 <xsl:template match='processing-instruction()'>
  <xsl:variable name="pitarget" select="name()"/>
  <xsl:processing-instruction name="{$pitarget}">
   <xsl:value-of select="."/>
  </xsl:processing-instruction>
 </xsl:template>

 <xsl:template match="/">
  <xsl:text>&#xA;</xsl:text>
  <xsl:apply-templates/>
  <xsl:text>&#xA;</xsl:text>
 </xsl:template>

 <xsl:template match="@*|*" name="identity">
  <xsl:copy>
   <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
 </xsl:template>
 
</xsl:stylesheet>

<!-- Keep this comment at the end of the file
Local variables:
mode: xml
sgml-default-dtd-file:"/SGML/Public/Emacs/xslt.ced"
sgml-omittag:t
sgml-shorttag:t
sgml-indent-data:t
sgml-indent-step:1
End:
-->

