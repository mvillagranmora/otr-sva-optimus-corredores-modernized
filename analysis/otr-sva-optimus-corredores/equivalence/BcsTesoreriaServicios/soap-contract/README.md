# Tesoreria SOAP contract (static capture)

Phase 2 criterion: "Tesoreria WSDL contract captured and committed as a contract-test artifact".

The legacy exposes two SOAP services to the bank BCP, `SvcRecaudaInqV1` and `SvcRecaudaTrnV1`
(namespace `http://mfxV2_0`, document/literal BARE, one operation each: `SvcRecaudaInq`, `SvcRecaudaTrn`).
There is no `.wsdl` in the repository (the wsdlLocation of the generated client points at a developer temp
file, `file:/C:/Users/rulloa/AppData/Local/Temp/.../SOGI_SvcRecauda*V1_1.wsdl`). The contract was therefore
captured STATICALLY from the wsimport/wsdl2java-generated sources.

## Files

| file | what |
|---|---|
| `SvcRecaudaInqV1.contract.json`, `SvcRecaudaTrnV1.contract.json` | deterministic contract per service (sorted keys, defaults filled in, every type cites legacy file:line) |
| `CONTRACT-SUMMARY.md` | human-readable tables generated from the JSON |
| `tool/soap_contract_extract.py` | the extractor (`py -3 -I`, no network, parses the legacy sources as text, nothing is compiled) |
| `tool/port_to_jakarta.py` | the mechanical javax -> jakarta port that produced the modern types |
| `../../../java/cl/bcs/tesoreria/soap/SoapContratoEstaticoTest.java` | compares the JSON with the ported classes by reflection (SOAP-01..SOAP-14) |

Regenerate (from the workspace root):

    py -3 -I modernized/otr-sva-optimus-corredores/BcsTesoreriaServicios/src/test/resources/soap/contract/tool/soap_contract_extract.py \
       --legacy-dir legacy/otr-sva-optimus-corredores/BSCL/Tesoreria/JAVA/BcsTesoreriaWebServices/src/cl/bcs/tesoreria/webservices/recaudacion \
       --legacy-root . --out-dir modernized/otr-sva-optimus-corredores/BcsTesoreriaServicios/src/test/resources/soap/contract

The tool fails on purpose on an annotation or attribute it does not model (for example `@XmlAnyElement`).
Re-running it on the same sources gives byte-identical output.

## What the capture contains

Service attributes (`@WebService`, `@WebServiceClient`, `@WebEndpoint`, `@SOAPBinding`), the port type
operation (`@WebMethod`, `@WebParam` name/targetNamespace/mode INOUT/partName, `Holder<...>`), all JAXB types
(`@XmlType` name/propOrder, `@XmlElement` name/required/nillable, `@XmlSchemaType`, `@XmlEnum`/`@XmlEnumValue`,
`@XmlSeeAlso`, List cardinalities), the `ObjectFactory` (`@XmlRegistry`, 129 `@XmlElementDecl`) and the package
`@XmlSchema` (namespace, elementFormDefault QUALIFIED).

## What a static capture does NOT prove

* No real WSDL was generated: there is no `wsgen`/CXF/JAX-WS runtime offline, so nothing here shows that a JAX-WS
  runtime would emit the same WSDL/XSD from these annotations (the legacy WSDL itself was never available).
* No wire-level XML: no SOAP envelope was marshalled or unmarshalled, element order, namespaces prefixes,
  `xsi:nil` handling, `xs:dateTime` formatting of `XMLGregorianCalendar`, enum text on the wire are inferred
  from annotations, not observed.
* No consumer validation: nothing was exchanged with, or validated against, the real BCP client or its WSDL/XSD.
* Annotation-level equality only: the test proves the modern classes carry the same annotations as the legacy
  generated classes. It does not prove behavior (see the gap list below), and it cannot detect a legacy contract
  that differs from the generated sources (for example a newer WSDL than the 2017-09-25 CXF 3.0.2 generation).
* `endpointInterface` is not wired in the modern endpoints (test SOAP-13 is a deliberate tripwire).

## Findings worth knowing before the real wiring

* The legacy `ObjectFactory` is incomplete for `SvcRecaudaInqV1`: it has `@XmlElementDecl`/`create...` for
  `MFX_SvcRecaudaTrn`, `SvcRecaudaTrnRq/Rs` but NOT for `MFX_SvcRecaudaInq`, `SvcRecaudaInqRq/Rs`
  (`MFXSvcRecaudaInqType`, `SvcRecaudaInqRqType`, `SvcRecaudaInqRsType` are reachable only through the
  operation parameter). With parameterStyle BARE the part `MFX_SvcRecaudaInq` needs a global element: how the
  runtime resolves it must be checked against the real WSDL. Inq contract = 61 types, Trn = 58.
* Polymorphism: `ClientAppType` has `@XmlSeeAlso(ClientProxyType)` and `ClientProxyType extends ClientAppType`
  (no extra properties). `@XmlSeeAlso(ObjectFactory)` on both port types.
* No `@XmlAnyElement`, `@XmlJavaTypeAdapter`, `@XmlRootElement` or `@XmlAttribute` anywhere. `TrnTpLstType.trnTp`
  is a `List<TrnTpType>` enum list with `@XmlSchemaType(name = "string")`.
* `MFXSvcRecaudaInqType`/`MFXSvcRecaudaTrnType.toString()` call `cl.bcs.utils.Utils.printJson` (kept in the port;
  the legacy logs the JSON of the whole message with it).
