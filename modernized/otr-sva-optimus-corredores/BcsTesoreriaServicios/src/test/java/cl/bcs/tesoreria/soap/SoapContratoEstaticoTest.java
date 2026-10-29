package cl.bcs.tesoreria.soap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Static SOAP contract test (Phase 2 criterion: "Tesoreria WSDL contract captured and committed as a
 * contract-test artifact").
 *
 * Oracle: src/test/resources/soap/contract/{SvcRecaudaInqV1,SvcRecaudaTrnV1}.contract.json, produced by
 * tool/soap_contract_extract.py from the legacy sources (legacy/otr-sva-optimus-corredores/BSCL/Tesoreria/JAVA/
 * BcsTesoreriaWebServices/src/cl/bcs/tesoreria/webservices/recaudacion/, never compiled). The JSON holds the
 * annotations as written in the legacy code with the jakarta/javax defaults filled in; this test reads the same
 * facts by reflection from the ported modern classes (cl.bcs.tesoreria.infrastructure.soap) and fails on ANY
 * difference. It proves annotation-level equality, NOT a generated WSDL, NOT wire XML, NOT consumer validation
 * (see soap/contract/README.md).
 *
 * Contract ids SOAP-NN (legacy file:line cited on each test; per-type lines are in the JSON "source" entries).
 */
class SoapContratoEstaticoTest {

    private static final String PKG = "cl.bcs.tesoreria.infrastructure.soap";
    private static final List<String> SERVICES = List.of("SvcRecaudaInqV1", "SvcRecaudaTrnV1");
    private static final Map<String, String> ENDPOINT_OF = Map.of(
            "SvcRecaudaInqV1", "SvcRecaudaInqV1Endpoint", "SvcRecaudaTrnV1", "SvcRecaudaTrnV1Endpoint");
    /** Annotation simple names modelled by the extractor (anything else from the SOAP APIs fails SOAP-12). */
    private static final Set<String> MODELLED = Set.of(
            "XmlType", "XmlRootElement", "XmlAccessorType", "XmlElement", "XmlAttribute", "XmlSchemaType", "XmlEnum",
            "XmlEnumValue", "XmlSeeAlso", "XmlRegistry", "XmlElementDecl", "XmlSchema", "WebService",
            "WebServiceClient", "WebEndpoint", "WebMethod", "WebParam", "WebResult", "SOAPBinding",
            "RequestWrapper", "ResponseWrapper", "Oneway");
    private static final Set<String> FORBIDDEN = Set.of(
            "XmlAnyElement", "XmlAnyAttribute", "XmlJavaTypeAdapter", "XmlElements", "XmlElementRef",
            "XmlElementRefs", "XmlMixed", "XmlValue", "XmlTransient", "XmlList", "XmlIDREF", "XmlID");
    /** Modern classes in the package that are NOT part of the legacy contract (hand-written skeleton). */
    private static final Set<String> NON_CONTRACT_CLASSES = Set.of(
            "SvcRecaudaInqV1Endpoint", "SvcRecaudaTrnV1Endpoint", "SvcRecaudaInqRequest", "SvcRecaudaInqResponse",
            "SvcRecaudaTrnRequest", "SvcRecaudaTrnResponse");

    private static final Map<String, JsonObject> CONTRACT = new TreeMap<>();

    @BeforeAll
    static void loadContracts() throws Exception {
        for (String s : SERVICES) {
            try (Reader r = new InputStreamReader(
                    SoapContratoEstaticoTest.class.getResourceAsStream("/soap/contract/" + s + ".contract.json"),
                    StandardCharsets.UTF_8)) {
                CONTRACT.put(s, JsonParser.parseReader(r).getAsJsonObject());
            }
        }
    }

    // ------------------------------------------------------------------------------------------
    // SOAP-01  endpoint implementation @WebService
    // legacy SvcRecaudaInqV1_SoapHttpPortImpl.java:40-50 / SvcRecaudaTrnV1_SoapHttpPortImpl.java:42-52
    // ------------------------------------------------------------------------------------------
    @Test
    @DisplayName("SOAP-01: endpoint @WebService (serviceName, portName, targetNamespace) equals the legacy *_SoapHttpPortImpl")
    void soap01_endpointWebService() throws Exception {
        List<String> d = new ArrayList<>();
        for (String s : SERVICES) {
            JsonObject impl = CONTRACT.get(s).getAsJsonObject("endpointImplementation");
            Map<String, Object> exp = asMap(impl.getAsJsonObject("annotations").get("WebService"));
            // endpointInterface is the one declared difference: modern endpoint does not implement the PortType yet (SOAP-13)
            exp.put("endpointInterface", "");
            Map<String, Object> act = anns(cls(ENDPOINT_OF.get(s))).get("WebService");
            diff(d, s + " endpoint @WebService", exp, act);
        }
        assertThat(d).as("SOAP-01 differences").isEmpty();
    }

    // ------------------------------------------------------------------------------------------
    // SOAP-02  port type (SEI) @WebService, @XmlSeeAlso, @SOAPBinding
    // legacy SvcRecaudaInqV1PortType.java:15-18 / SvcRecaudaTrnV1PortType.java:15-18
    // ------------------------------------------------------------------------------------------
    @Test
    @DisplayName("SOAP-02: PortType interface annotations (@WebService name/ns, @XmlSeeAlso, @SOAPBinding BARE)")
    void soap02_portTypeAnnotations() throws Exception {
        List<String> d = new ArrayList<>();
        for (String s : SERVICES) {
            JsonObject sei = CONTRACT.get(s).getAsJsonObject("portType");
            Class<?> c = cls(sei.get("class").getAsString());
            if (!c.isInterface()) {
                d.add(s + ": " + c + " is not an interface");
            }
            diff(d, s + " PortType annotations", asMap(sei.get("annotations")), anns(c));
        }
        assertThat(d).as("SOAP-02 differences").isEmpty();
    }

    // ------------------------------------------------------------------------------------------
    // SOAP-03  operations: @WebMethod, @WebParam, return, throws, signature
    // legacy SvcRecaudaInqV1PortType.java:20-26 (SvcRecaudaInq), SvcRecaudaTrnV1PortType.java:20-26 (SvcRecaudaTrn)
    // ------------------------------------------------------------------------------------------
    @Test
    @DisplayName("SOAP-03: operation signatures (operationName/action, INOUT Holder param name/ns/partName)")
    void soap03_operations() throws Exception {
        List<String> d = new ArrayList<>();
        for (String s : SERVICES) {
            JsonObject sei = CONTRACT.get(s).getAsJsonObject("portType");
            Class<?> c = cls(sei.get("class").getAsString());
            JsonArray ops = sei.getAsJsonArray("operations");
            List<String> expNames = new ArrayList<>();
            ops.forEach(o -> expNames.add(o.getAsJsonObject().get("javaMethod").getAsString()));
            List<String> actNames = Arrays.stream(c.getDeclaredMethods()).map(Method::getName).sorted().toList();
            if (!expNames.equals(actNames)) {
                d.add(s + " operations expected " + expNames + " but was " + actNames);
            }
            for (JsonElement oe : ops) {
                JsonObject o = oe.getAsJsonObject();
                String jm = o.get("javaMethod").getAsString();
                Method m = Arrays.stream(c.getDeclaredMethods()).filter(x -> x.getName().equals(jm)).findFirst().orElse(null);
                if (m == null) {
                    continue;
                }
                String ctx = s + "." + jm;
                diff(d, ctx + " method annotations", asMap(o.get("annotations")), anns(m));
                eq(d, ctx + " returns", o.get("returns").getAsString(), simpleType(m.getGenericReturnType()));
                List<Object> thr = new ArrayList<>();
                Arrays.stream(m.getGenericExceptionTypes()).forEach(t -> thr.add(simpleType(t)));
                eq(d, ctx + " throws", asList(o.get("throws")), thr);
                JsonArray ps = o.getAsJsonArray("params");
                eq(d, ctx + " param count", ps.size(), m.getParameterCount());
                for (int i = 0; i < Math.min(ps.size(), m.getParameterCount()); i++) {
                    JsonObject p = ps.get(i).getAsJsonObject();
                    java.lang.reflect.Parameter ap = m.getParameters()[i];
                    String pc = ctx + " param#" + i;
                    eq(d, pc + " javaType", p.get("javaType").getAsString(), simpleType(m.getGenericParameterTypes()[i]));
                    eq(d, pc + " holder", p.get("holder").getAsBoolean(), ap.getType().getSimpleName().equals("Holder"));
                    diff(d, pc + " annotations", asMap(p.get("annotations")), anns(ap));
                }
            }
        }
        assertThat(d).as("SOAP-03 differences").isEmpty();
    }

    // ------------------------------------------------------------------------------------------
    // SOAP-04  service client class: @WebServiceClient, QNames, @WebEndpoint, default WSDL location
    // legacy SvcRecaudaInqV1.java:17-20, 22-35 (QNames/URL), 81-82, 98-99 (@WebEndpoint)
    // ------------------------------------------------------------------------------------------
    @Test
    @DisplayName("SOAP-04: service class @WebServiceClient, QName constants, @WebEndpoint getters, WSDL_LOCATION")
    void soap04_serviceClient() throws Exception {
        List<String> d = new ArrayList<>();
        for (String s : SERVICES) {
            JsonObject cl = CONTRACT.get(s).getAsJsonObject("serviceClient");
            Class<?> c = cls(cl.get("class").getAsString());
            diff(d, s + " service annotations", asMap(cl.get("annotations")), anns(c));
            eq(d, s + " service extends", cl.get("extends").getAsString(), c.getSuperclass().getSimpleName());
            Map<String, Object> expQ = asMap(cl.get("qnames"));
            Map<String, Object> actQ = new TreeMap<>();
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) && f.getType().getSimpleName().equals("QName")) {
                    f.setAccessible(true);
                    Object q = f.get(null);
                    actQ.put(f.getName(), List.of(
                            (String) q.getClass().getMethod("getNamespaceURI").invoke(q),
                            (String) q.getClass().getMethod("getLocalPart").invoke(q)));
                }
            }
            diff(d, s + " QName constants", expQ, actQ);
            Field wl = c.getDeclaredField("WSDL_LOCATION");
            Object url = wl.get(null);
            eq(d, s + " default WSDL_LOCATION", cl.get("defaultWsdlUrl").getAsString(), url == null ? null : url.toString());
            List<Object> expE = new ArrayList<>();
            for (JsonElement e : cl.getAsJsonArray("webEndpoints")) {
                JsonObject o = e.getAsJsonObject();
                expE.add(o.get("javaMethod").getAsString() + "|" + o.get("name").getAsString() + "|"
                        + o.get("returns").getAsString() + "|" + o.get("paramCount").getAsInt());
            }
            List<Object> actE = new ArrayList<>();
            for (Method m : c.getDeclaredMethods()) {
                jakarta.xml.ws.WebEndpoint we = m.getAnnotation(jakarta.xml.ws.WebEndpoint.class);
                if (we != null) {
                    actE.add(m.getName() + "|" + we.name() + "|" + simpleType(m.getGenericReturnType()) + "|"
                            + m.getParameterCount());
                }
            }
            actE.sort(Comparator.comparing(Object::toString));
            expE.sort(Comparator.comparing(Object::toString));
            eq(d, s + " @WebEndpoint getters", expE, actE);
        }
        assertThat(d).as("SOAP-04 differences").isEmpty();
    }

    // ------------------------------------------------------------------------------------------
    // SOAP-05  package-info @XmlSchema (namespace + elementFormDefault)
    // legacy package-info.java:1
    // ------------------------------------------------------------------------------------------
    @Test
    @DisplayName("SOAP-05: package-info @XmlSchema namespace http://mfxV2_0, elementFormDefault QUALIFIED")
    void soap05_packageSchema() throws Exception {
        List<String> d = new ArrayList<>();
        for (String s : SERVICES) {
            JsonObject pkg = CONTRACT.get(s).getAsJsonObject("package");
            Package p = cls("ObjectFactory").getPackage();
            Map<String, Object> act = new TreeMap<>();
            for (Annotation a : p.getDeclaredAnnotations()) {
                if (relevant(a)) {
                    act.put(a.annotationType().getSimpleName(), annMap(a));
                }
            }
            diff(d, s + " package annotations", asMap(pkg.get("annotations")), act);
            eq(d, s + " package name", pkg.get("name").getAsString().replace("cl.bcs.tesoreria.webservices.recaudacion", PKG), p.getName());
        }
        assertThat(d).as("SOAP-05 differences").isEmpty();
    }

    // ------------------------------------------------------------------------------------------
    // SOAP-06  type set: every contract type exists, and no extra @XmlType class exists
    // legacy: the 61 wsimport types of webservices/recaudacion (JSON types[].source gives file:line)
    // ------------------------------------------------------------------------------------------
    @Test
    @DisplayName("SOAP-06: the set of JAXB types equals the legacy set (no missing, no extra)")
    void soap06_typeSet() throws Exception {
        List<String> d = new ArrayList<>();
        Set<String> union = new TreeSet<>();
        for (String s : SERVICES) {
            for (JsonElement t : CONTRACT.get(s).getAsJsonArray("types")) {
                String n = t.getAsJsonObject().get("name").getAsString();
                union.add(n);
                try {
                    cls(n);
                } catch (ClassNotFoundException e) {
                    d.add(s + ": missing modern type " + n);
                }
            }
        }
        Set<String> modern = new TreeSet<>();
        Set<String> others = new TreeSet<>();
        for (String n : modernClassNames()) {
            Class<?> c = cls(n);
            if (c.isAnnotationPresent(jakarta.xml.bind.annotation.XmlType.class)) {
                modern.add(n);
            } else {
                others.add(n);
            }
        }
        eq(d, "JAXB @XmlType classes (modern) vs contract union", new ArrayList<>(union), new ArrayList<>(modern));
        Set<String> expectedOthers = new TreeSet<>(NON_CONTRACT_CLASSES);
        for (String s : SERVICES) {
            JsonObject c = CONTRACT.get(s);
            expectedOthers.add(c.getAsJsonObject("portType").get("class").getAsString());
            expectedOthers.add(c.getAsJsonObject("serviceClient").get("class").getAsString());
            expectedOthers.add("ObjectFactory");
        }
        eq(d, "non-JAXB classes in the soap package", new ArrayList<>(expectedOthers), new ArrayList<>(others));
        assertThat(d).as("SOAP-06 differences").isEmpty();
    }

    // ------------------------------------------------------------------------------------------
    // SOAP-07  type level: @XmlType name/namespace/propOrder, @XmlAccessorType, @XmlSeeAlso, @XmlEnum, @XmlRootElement,
    //          superclass (polymorphism: ClientProxyType extends ClientAppType, ClientAppType.java:@XmlSeeAlso)
    // legacy: JSON types[].source (e.g. SvcRecaudaInqRqType.java:36-42)
    // ------------------------------------------------------------------------------------------
    @Test
    @DisplayName("SOAP-07: @XmlType name/namespace/propOrder, accessor type, @XmlSeeAlso, @XmlEnum, superclass of every type")
    void soap07_typeLevel() throws Exception {
        List<String> d = new ArrayList<>();
        for (String s : SERVICES) {
            JsonObject root = CONTRACT.get(s);
            String pkgNs = root.getAsJsonObject("package").get("namespace").getAsString();
            for (JsonElement te : root.getAsJsonArray("types")) {
                JsonObject t = te.getAsJsonObject();
                String n = t.get("name").getAsString();
                Class<?> c = cls(n);
                String ctx = s + "." + n;
                eq(d, ctx + " kind", t.get("kind").getAsString(), kind(c));
                diff(d, ctx + " class annotations", asMap(t.get("annotations")), anns(c));
                Class<?> sup = c.getSuperclass();
                String supName = sup == null || sup == Object.class || sup == Enum.class ? null : sup.getSimpleName();
                eq(d, ctx + " extends", t.get("extends").isJsonNull() ? null : t.get("extends").getAsString(), supName);
                jakarta.xml.bind.annotation.XmlType xt = c.getAnnotation(jakarta.xml.bind.annotation.XmlType.class);
                if (xt != null) {
                    String ns = xt.namespace().equals("##default") ? pkgNs : xt.namespace();
                    eq(d, ctx + " effective @XmlType namespace", t.get("effectiveXmlTypeNamespace").getAsString(), ns);
                    if (!c.isEnum()) {
                        List<String> order = Arrays.stream(xt.propOrder()).filter(x -> !x.isEmpty()).toList();
                        List<String> fields = Arrays.stream(c.getDeclaredFields())
                                .filter(f -> !Modifier.isStatic(f.getModifiers()) && !f.isSynthetic())
                                .map(Field::getName).sorted().toList();
                        eq(d, ctx + " propOrder covers exactly the fields", fields,
                                order.stream().sorted().toList());
                    }
                }
            }
        }
        assertThat(d).as("SOAP-07 differences").isEmpty();
    }

    // ------------------------------------------------------------------------------------------
    // SOAP-08  properties: @XmlElement(name, namespace, required, nillable), @XmlSchemaType, @XmlAttribute, Java type
    // legacy: JSON types[].properties[].line (e.g. SvcRecaudaInqRqType.java:44-51)
    // ------------------------------------------------------------------------------------------
    @Test
    @DisplayName("SOAP-08: every property keeps element name/namespace/required/nillable/schema type/Java type")
    void soap08_properties() throws Exception {
        List<String> d = new ArrayList<>();
        int checked = 0;
        for (String s : SERVICES) {
            JsonObject root = CONTRACT.get(s);
            String pkgNs = root.getAsJsonObject("package").get("namespace").getAsString();
            boolean qualified = root.getAsJsonObject("package").get("elementFormDefault").getAsString().equals("QUALIFIED");
            for (JsonElement te : root.getAsJsonArray("types")) {
                JsonObject t = te.getAsJsonObject();
                if (!t.get("kind").getAsString().equals("class")) {
                    continue;
                }
                Class<?> c = cls(t.get("name").getAsString());
                Map<String, Field> actual = new TreeMap<>();
                for (Field f : c.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) && !f.isSynthetic()) {
                        actual.put(f.getName(), f);
                    }
                }
                Set<String> expNames = new TreeSet<>();
                for (JsonElement pe : t.getAsJsonArray("properties")) {
                    JsonObject p = pe.getAsJsonObject();
                    String fn = p.get("field").getAsString();
                    expNames.add(fn);
                    String ctx = s + "." + c.getSimpleName() + "." + fn;
                    Field f = actual.get(fn);
                    if (f == null) {
                        d.add(ctx + ": field missing");
                        continue;
                    }
                    checked++;
                    Prop a = prop(f, pkgNs, qualified);
                    eq(d, ctx + " javaType", p.get("javaType").getAsString(), a.javaType);
                    eq(d, ctx + " collection", p.get("collection").isJsonNull() ? null : p.get("collection").getAsString(), a.collection);
                    eq(d, ctx + " primitive", p.get("primitive").getAsBoolean(), a.primitive);
                    eq(d, ctx + " kind", p.get("kind").getAsString(), a.kind);
                    eq(d, ctx + " xml name", p.get("xmlName").getAsString(), a.xmlName);
                    eq(d, ctx + " effective namespace", p.get("effectiveNamespace").getAsString(), a.namespace);
                    diff(d, ctx + " annotations", asMap(p.get("annotations")), anns(f));
                }
                if (!expNames.equals(actual.keySet())) {
                    d.add(s + "." + c.getSimpleName() + " fields expected " + expNames + " but was " + actual.keySet());
                }
            }
        }
        assertThat(checked).as("properties compared").isGreaterThan(150);
        assertThat(d).as("SOAP-08 differences").isEmpty();
    }

    // ------------------------------------------------------------------------------------------
    // SOAP-09  cardinality: lists (List<T>, minOccurs from required), optional 0..1, required 1..1, JAXB live-list getter
    // legacy: the 10 *LstType list properties (e.g. TrnTpLstType.java:39-40, getter :64)
    // ------------------------------------------------------------------------------------------
    @Test
    @DisplayName("SOAP-09: cardinality of every property (0..1, 1..1, 0..n, 1..n) and list getters without setter")
    void soap09_cardinality() throws Exception {
        List<String> d = new ArrayList<>();
        int lists = 0;
        for (String s : SERVICES) {
            JsonObject root = CONTRACT.get(s);
            String pkgNs = root.getAsJsonObject("package").get("namespace").getAsString();
            boolean qualified = root.getAsJsonObject("package").get("elementFormDefault").getAsString().equals("QUALIFIED");
            for (JsonElement te : root.getAsJsonArray("types")) {
                JsonObject t = te.getAsJsonObject();
                if (!t.get("kind").getAsString().equals("class")) {
                    continue;
                }
                Class<?> c = cls(t.get("name").getAsString());
                for (JsonElement pe : t.getAsJsonArray("properties")) {
                    JsonObject p = pe.getAsJsonObject();
                    String fn = p.get("field").getAsString();
                    String ctx = s + "." + c.getSimpleName() + "." + fn;
                    Field f;
                    try {
                        f = c.getDeclaredField(fn);
                    } catch (NoSuchFieldException e) {
                        d.add(ctx + ": field missing");
                        continue;
                    }
                    eq(d, ctx + " cardinality", p.get("cardinality").getAsString(), prop(f, pkgNs, qualified).cardinality);
                    if (!p.get("collection").isJsonNull()) {
                        lists++;
                        String cap = Character.toUpperCase(fn.charAt(0)) + fn.substring(1);
                        try {
                            Method g = c.getMethod("get" + cap);
                            eq(d, ctx + " live-list getter returns", "List<" + p.get("javaType").getAsString() + ">",
                                    simpleType(g.getGenericReturnType()));
                        } catch (NoSuchMethodException e) {
                            d.add(ctx + ": list getter missing");
                        }
                        boolean hasSetter = Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals("set" + cap));
                        eq(d, ctx + " has no setter (JAXB live list)", false, hasSetter);
                    }
                }
            }
        }
        assertThat(lists).as("list properties compared (per service, summed)").isEqualTo(20);
        assertThat(d).as("SOAP-09 differences").isEmpty();
    }

    // ------------------------------------------------------------------------------------------
    // SOAP-10  enums: constants, order, @XmlEnumValue, value()/fromValue()
    // legacy: SegRolType.java:26-45, SeveridadType.java and the other 4 *Type enums (JSON enumValues[].line)
    // ------------------------------------------------------------------------------------------
    @Test
    @DisplayName("SOAP-10: enum constants, order and wire values (@XmlEnumValue) of the 6 enums")
    void soap10_enums() throws Exception {
        List<String> d = new ArrayList<>();
        int enums = 0;
        for (String s : SERVICES) {
            for (JsonElement te : CONTRACT.get(s).getAsJsonArray("types")) {
                JsonObject t = te.getAsJsonObject();
                if (!t.get("kind").getAsString().equals("enum")) {
                    continue;
                }
                enums++;
                Class<?> c = cls(t.get("name").getAsString());
                String ctx = s + "." + c.getSimpleName();
                if (!c.isEnum()) {
                    d.add(ctx + " is not an enum");
                    continue;
                }
                List<Object> exp = new ArrayList<>();
                for (JsonElement v : t.getAsJsonArray("enumValues")) {
                    exp.add(v.getAsJsonObject().get("constant").getAsString() + "=" + v.getAsJsonObject().get("xmlValue").getAsString());
                }
                List<Object> act = new ArrayList<>();
                for (Object k : c.getEnumConstants()) {
                    Enum<?> en = (Enum<?>) k;
                    jakarta.xml.bind.annotation.XmlEnumValue ev =
                            c.getField(en.name()).getAnnotation(jakarta.xml.bind.annotation.XmlEnumValue.class);
                    act.add(en.name() + "=" + (ev == null ? en.name() : ev.value()));
                    String wire = (String) c.getMethod("value").invoke(en);
                    eq(d, ctx + " value() of " + en.name(), ev == null ? en.name() : ev.value(), wire);
                    eq(d, ctx + " fromValue(value()) of " + en.name(), en, c.getMethod("fromValue", String.class).invoke(null, wire));
                }
                eq(d, ctx + " constants=wire values (ordered)", exp, act);
            }
        }
        assertThat(enums).as("enums compared (per service, summed)").isEqualTo(12);
        assertThat(d).as("SOAP-10 differences").isEmpty();
    }

    // ------------------------------------------------------------------------------------------
    // SOAP-11  ObjectFactory: @XmlRegistry, 129 @XmlElementDecl, createXxx factories
    // legacy ObjectFactory.java:26-27 (registry), :33-.. (QNames), :667 (MFX_SvcRecaudaTrn decl)
    // ------------------------------------------------------------------------------------------
    @Test
    @DisplayName("SOAP-11: ObjectFactory @XmlRegistry, every @XmlElementDecl (name, ns, scope, value type) and createXxx method")
    void soap11_objectFactory() throws Exception {
        List<String> d = new ArrayList<>();
        Class<?> of = cls("ObjectFactory");
        for (String s : SERVICES) {
            JsonObject o = CONTRACT.get(s).getAsJsonObject("objectFactory");
            diff(d, s + " ObjectFactory class annotations", asMap(o.get("annotations")), anns(of));
            List<Object> expDecl = new ArrayList<>();
            for (JsonElement e : o.getAsJsonArray("elementDecls")) {
                JsonObject x = e.getAsJsonObject();
                expDecl.add(x.get("method").getAsString() + "|" + x.get("namespace").getAsString() + "|"
                        + x.get("name").getAsString() + "|" + x.get("scope").getAsString() + "|"
                        + x.get("substitutionHeadName").getAsString() + "|JAXBElement<" + x.get("valueType").getAsString()
                        + ">|" + asList(x.get("paramTypes")));
            }
            List<Object> expCreate = new ArrayList<>();
            for (JsonElement e : o.getAsJsonArray("createMethods")) {
                expCreate.add(e.getAsJsonObject().get("method").getAsString() + "|" + e.getAsJsonObject().get("returns").getAsString());
            }
            List<Object> actDecl = new ArrayList<>();
            List<Object> actCreate = new ArrayList<>();
            for (Method m : of.getDeclaredMethods()) {
                jakarta.xml.bind.annotation.XmlElementDecl ed = m.getAnnotation(jakarta.xml.bind.annotation.XmlElementDecl.class);
                if (ed != null) {
                    List<Object> pt = new ArrayList<>();
                    Arrays.stream(m.getGenericParameterTypes()).forEach(t -> pt.add(simpleType(t)));
                    actDecl.add(m.getName() + "|" + ed.namespace() + "|" + ed.name() + "|" + ed.scope().getSimpleName() + "|"
                            + ed.substitutionHeadName() + "|" + simpleType(m.getGenericReturnType()) + "|" + pt);
                } else if (m.getName().startsWith("create")) {
                    actCreate.add(m.getName() + "|" + simpleType(m.getGenericReturnType()));
                }
            }
            expDecl.sort(Comparator.comparing(Object::toString));
            actDecl.sort(Comparator.comparing(Object::toString));
            expCreate.sort(Comparator.comparing(Object::toString));
            actCreate.sort(Comparator.comparing(Object::toString));
            eq(d, s + " @XmlElementDecl set", expDecl, actDecl);
            eq(d, s + " createXxx set", expCreate, actCreate);
            // the extractor sorted by (name, method); also pin the counts reported by the capture
            eq(d, s + " element decl count", o.getAsJsonArray("elementDecls").size(), 129);
        }
        assertThat(d).as("SOAP-11 differences").isEmpty();
    }

    // ------------------------------------------------------------------------------------------
    // SOAP-12  no annotation of the SOAP APIs outside what the extractor models; none of the shape-changing ones
    //          (@XmlAnyElement, adapters, @XmlTransient, ...) were in the legacy and must not appear in the port
    // ------------------------------------------------------------------------------------------
    @Test
    @DisplayName("SOAP-12: the ported classes use only modelled JAXB/JAX-WS annotations (no @XmlAnyElement & co.)")
    void soap12_noUnmodelledAnnotations() throws Exception {
        List<String> d = new ArrayList<>();
        for (String n : modernClassNames()) {
            if (NON_CONTRACT_CLASSES.contains(n)) {
                continue;
            }
            Class<?> c = cls(n);
            List<AnnotatedElement> els = new ArrayList<>();
            els.add(c);
            els.addAll(Arrays.asList(c.getDeclaredFields()));
            els.addAll(Arrays.asList(c.getDeclaredMethods()));
            for (Method m : c.getDeclaredMethods()) {
                els.addAll(Arrays.asList(m.getParameters()));
            }
            for (AnnotatedElement e : els) {
                for (Annotation a : e.getDeclaredAnnotations()) {
                    String an = a.annotationType().getSimpleName();
                    if (relevant(a) && (FORBIDDEN.contains(an) || !MODELLED.contains(an))) {
                        d.add(n + ": unmodelled @" + an + " on " + e);
                    }
                }
            }
        }
        assertThat(d).as("SOAP-12 differences").isEmpty();
    }

    // ------------------------------------------------------------------------------------------
    // SOAP-13  KNOWN GAP tripwire: the modern endpoints do not implement the PortType (Holder<MFX..> signature) and carry
    //          no endpointInterface; the legacy impl does (SvcRecaudaInqV1_SoapHttpPortImpl.java:44,50;
    //          SvcRecaudaTrnV1_SoapHttpPortImpl.java:46,52). Real SOAP wiring is a declared blocker. When someone wires it,
    //          this test fails on purpose: replace it by a strict endpointInterface comparison in SOAP-01.
    // ------------------------------------------------------------------------------------------
    @Test
    @DisplayName("SOAP-13: KNOWN GAP - endpoints do not implement the PortType / declare endpointInterface yet")
    void soap13_knownGapEndpointInterface() throws Exception {
        List<String> d = new ArrayList<>();
        for (String s : SERVICES) {
            JsonObject c = CONTRACT.get(s);
            String sei = c.getAsJsonObject("portType").get("class").getAsString();
            String legacyEi = c.getAsJsonObject("endpointImplementation").getAsJsonObject("annotations")
                    .getAsJsonObject("WebService").get("endpointInterface").getAsString();
            eq(d, s + " legacy endpointInterface points at the PortType", true, legacyEi.endsWith("." + sei));
            eq(d, s + " legacy impl implements the PortType", List.of(sei), asList(c.getAsJsonObject("endpointImplementation").get("implements")));
            Class<?> ep = cls(ENDPOINT_OF.get(s));
            eq(d, s + " modern endpoint implements PortType (gap: still false)", false, cls(sei).isAssignableFrom(ep));
            eq(d, s + " modern @WebService.endpointInterface (gap: still empty)", "",
                    ep.getAnnotation(jakarta.jws.WebService.class).endpointInterface());
        }
        assertThat(d).as("SOAP-13 gap changed: update the test and the README").isEmpty();
    }

    // ------------------------------------------------------------------------------------------
    // SOAP-14  the captured artifact itself: version, honesty markers, sizes, citations
    // ------------------------------------------------------------------------------------------
    @Test
    @DisplayName("SOAP-14: the JSON contracts are well formed, cite legacy file:line and declare what they do not prove")
    void soap14_artifactSanity() {
        List<String> d = new ArrayList<>();
        for (String s : SERVICES) {
            JsonObject c = CONTRACT.get(s);
            eq(d, s + " contractVersion", 1, c.get("contractVersion").getAsInt());
            eq(d, s + " service", s, c.get("service").getAsString());
            eq(d, s + " operations", 1, c.getAsJsonObject("portType").getAsJsonArray("operations").size());
            eq(d, s + " stats.typeCount", c.getAsJsonObject("stats").get("typeCount").getAsInt(), c.getAsJsonArray("types").size());
            eq(d, s + " namespace", "http://mfxV2_0", c.getAsJsonObject("package").get("namespace").getAsString());
            eq(d, s + " doesNotProve declared", 3, c.getAsJsonObject("extraction").getAsJsonArray("doesNotProve").size());
            for (JsonElement t : c.getAsJsonArray("types")) {
                JsonObject src = t.getAsJsonObject().getAsJsonObject("source");
                if (!src.get("file").getAsString().startsWith("legacy/") || src.get("line").getAsInt() < 1) {
                    d.add(s + " " + t.getAsJsonObject().get("name") + " lacks a legacy file:line citation");
                }
            }
        }
        eq(d, "Inq type count (ObjectFactory of the legacy has no MFX_SvcRecaudaInq/Rq/Rs registration)", 61,
                CONTRACT.get("SvcRecaudaInqV1").getAsJsonArray("types").size());
        eq(d, "Trn type count", 58, CONTRACT.get("SvcRecaudaTrnV1").getAsJsonArray("types").size());
        assertThat(d).as("SOAP-14 differences").isEmpty();
    }

    // ==========================================================================================
    // reflection helpers (same extraction rules as tool/soap_contract_extract.py)
    // ==========================================================================================

    private static final class Prop {
        String javaType;
        String collection;
        boolean primitive;
        String kind;
        String xmlName;
        String namespace;
        String cardinality;
    }

    private static Prop prop(Field f, String pkgNs, boolean qualified) {
        Prop p = new Prop();
        Type gt = f.getGenericType();
        if (gt instanceof ParameterizedType pt && f.getType() == List.class) {
            p.collection = "List";
            p.javaType = simpleType(pt.getActualTypeArguments()[0]);
        } else {
            p.javaType = simpleType(gt);
        }
        p.primitive = f.getType().isPrimitive();
        jakarta.xml.bind.annotation.XmlElement el = f.getAnnotation(jakarta.xml.bind.annotation.XmlElement.class);
        jakarta.xml.bind.annotation.XmlAttribute at = f.getAnnotation(jakarta.xml.bind.annotation.XmlAttribute.class);
        boolean coll = p.collection != null;
        if (el != null) {
            p.kind = "element";
            p.xmlName = el.name().equals("##default") ? f.getName() : el.name();
            p.namespace = el.namespace().equals("##default") ? (qualified ? pkgNs : "") : el.namespace();
            p.cardinality = coll ? (el.required() ? "1..n" : "0..n") : p.primitive ? "1..1" : (el.required() ? "1..1" : "0..1");
        } else if (at != null) {
            p.kind = "attribute";
            p.xmlName = at.name().equals("##default") ? f.getName() : at.name();
            p.namespace = at.namespace().equals("##default") ? "" : at.namespace();
            p.cardinality = at.required() ? "1..1" : "0..1";
        } else {
            p.kind = "unannotated";
            p.xmlName = f.getName();
            p.namespace = qualified ? pkgNs : "";
            p.cardinality = coll ? "0..n" : p.primitive ? "1..1" : "0..1";
        }
        return p;
    }

    private static Class<?> cls(String simple) throws ClassNotFoundException {
        return Class.forName(PKG + "." + simple);
    }

    private static String kind(Class<?> c) {
        return c.isEnum() ? "enum" : c.isInterface() ? "interface" : "class";
    }

    private static List<String> modernClassNames() throws Exception {
        URL u = SoapContratoEstaticoTest.class.getClassLoader().getResource(PKG.replace('.', '/'));
        assertThat(u).as("soap package on the classpath").isNotNull();
        Path dir = Paths.get(u.toURI());
        try (Stream<Path> st = Files.list(dir)) {
            return st.map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(".class") && !n.contains("$") && !n.equals("package-info.class"))
                    .map(n -> n.substring(0, n.length() - 6)).sorted().toList();
        }
    }

    private static boolean relevant(Annotation a) {
        String p = a.annotationType().getName();
        return p.startsWith("jakarta.xml.bind.") || p.startsWith("jakarta.jws.") || p.startsWith("jakarta.xml.ws.");
    }

    private static Map<String, Map<String, Object>> anns(AnnotatedElement e) {
        Map<String, Map<String, Object>> m = new TreeMap<>();
        for (Annotation a : e.getDeclaredAnnotations()) {
            if (relevant(a)) {
                m.put(a.annotationType().getSimpleName(), annMap(a));
            }
        }
        return m;
    }

    private static Map<String, Object> annMap(Annotation a) {
        Map<String, Object> m = new TreeMap<>();
        try {
            for (Method x : a.annotationType().getDeclaredMethods()) {
                if (x.getParameterCount() == 0 && !Modifier.isStatic(x.getModifiers())) {
                    m.put(x.getName(), actualValue(x.invoke(a)));
                }
            }
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
        return m;
    }

    private static Object actualValue(Object v) {
        if (v instanceof Class<?> c) {
            return c.getSimpleName();
        }
        if (v instanceof Enum<?> e) {
            return e.name();
        }
        if (v instanceof Annotation a) {
            return annMap(a);
        }
        if (v != null && v.getClass().isArray()) {
            List<Object> l = new ArrayList<>();
            for (int i = 0; i < Array.getLength(v); i++) {
                l.add(actualValue(Array.get(v, i)));
            }
            return l;
        }
        return v;
    }

    private static Object plain(JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }
        if (e.isJsonObject()) {
            Map<String, Object> m = new TreeMap<>();
            e.getAsJsonObject().entrySet().forEach(en -> m.put(en.getKey(), plain(en.getValue())));
            return m;
        }
        if (e.isJsonArray()) {
            List<Object> l = new ArrayList<>();
            e.getAsJsonArray().forEach(x -> l.add(plain(x)));
            return l;
        }
        var p = e.getAsJsonPrimitive();
        return p.isBoolean() ? (Object) p.getAsBoolean() : p.isNumber() ? (Object) p.getAsLong() : p.getAsString();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(JsonElement e) {
        return (Map<String, Object>) plain(e);
    }

    @SuppressWarnings("unchecked")
    private static List<Object> asList(JsonElement e) {
        return (List<Object>) plain(e);
    }

    private static String simpleType(Type t) {
        return t.getTypeName().replaceAll("\\s+", "")
                .replaceAll("(?:[A-Za-z_][A-Za-z0-9_]*\\.)+(?=[A-Za-z_])", "");
    }

    private static void eq(List<String> d, String what, Object expected, Object actual) {
        Object e = expected instanceof Number n ? (Object) n.longValue() : expected;
        Object a = actual instanceof Number n ? (Object) n.longValue() : actual;
        if (!java.util.Objects.equals(e, a)) {
            d.add(what + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }

    /** Deep compare of two annotation-like maps (nested maps/lists), reporting every differing leaf. */
    private static void diff(List<String> d, String ctx, Object expected, Object actual) {
        if (expected instanceof Map<?, ?> em && actual instanceof Map<?, ?> am) {
            Set<Object> keys = new TreeSet<>(Comparator.comparing(Object::toString));
            keys.addAll(em.keySet());
            keys.addAll(am.keySet());
            for (Object k : keys) {
                if (!em.containsKey(k)) {
                    d.add(ctx + "." + k + ": unexpected in modern (absent in legacy contract) -> <" + am.get(k) + ">");
                } else if (!am.containsKey(k)) {
                    d.add(ctx + "." + k + ": missing in modern, legacy has <" + em.get(k) + ">");
                } else {
                    diff(d, ctx + "." + k, em.get(k), am.get(k));
                }
            }
        } else if (!java.util.Objects.equals(expected, actual)) {
            d.add(ctx + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
