package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;


import cl.bcs.normativa.data.Registro;

/**
 * Implementaci&oacute;n del validador de la DJ1862.
 *
 * 
 *
 */
public class ValidadorDJ1862 extends Validador {

	private static final List<String> CODIGOS_OPERACIONES_CAMBIOS_INTERNACIONALES = Arrays.asList("10000","10002","10005","10007","10008","10020",
			"10024","10025","10028","10040","10050","10051","10052","10053","10055","10057","10060","10070","10075","10080","10081","10090",
			"10096","10100","10400","10425","10450","10460","10480","10570","10700","10750","10822","10830","10900","10950","11100","11200",
			"11201","11210","11211","11215","11216","11220","11221","11225","11226","11230","11231","11240","11241","11242","11245","11246",
			"11250","11251","11260","11261","11270","11271","11280","11281","11290","11400","11403","11405","11410","11415","11420","11450",
			"11460","11465","11470","11475","11480","11485","11490","11495","20000","20002","20005","20007","20008","20020","20021","20023",
			"20024","20025","20026","20028","20040","20050","20051","20052","20053","20055","20057","20060","20070","20075","20077","20080",
			"20081","20090","20096","20100","20420","20450","20470","20500","20525","20551","20580","20700","20750","20830","20900","20950",
			"21100","21101","21200","21210","21215","21220","21225","21230","21240","21245","21250","21260","21270","21280","21290","21295",
			"21400","21401","21402","21403","21405","21406","21410","21411","21415","21420","21421","21450","21451","21460","21461","21465",
			"21466","21470","21471","21475","21476","21480","21481","21485","21486","21490","21491","21498","21499");
	private static final List<String> MODALIDAD_OPERACION = Arrays.asList("SW", "TX", "CH", "OT");
    // Codigos de moneda DJ1862
    private static final List<String> MONEDA = Arrays.asList("143","134","4","153","51","96","113","135","139","36","6","13","136","154",
            "127","142","902","156","82","23","102","149","138","24","981","982","900","1","999","129","150","132","980","26","128","5",
            "152","155","137","151","144","72","48","998","2");

    // Codigos de paises DJ1862
    private static final List<String> PAISES = Arrays.asList("308","518","563","152","525","140","251","240","271","302","127","224","540",
            "272","151","406","509","541","207","313","441","321","204","153","542","514","236","150","252","318","221","154","543","113",
            "220","344","527","161","141","129","315","149","226","130","997","336","305","162","524","461","202","118","143","144","334",
            "333","211","107","547","209","507","155","231","218","124","213","341","163","545","548","517","225","549","139","401","335",
            "512","505","145","582","102","550","108","591","232","230","261","442","215","262","147","104","103","217","208","520","214",
            "342","530","317","328","510","309","307","506","584","431","592","432","516","253","471","350","593","600","257","433","258",
            "443","164","418","256","282","254","306","504","205","331","444","445","301","551","137","446","552","416","303","316","114",
            "553","311","106","125","534","554","532","345","555","120","329","115","327","133","523","128","263","119","134","216","417",
            "447","556","535","337","255","121","326","159","402","320","212","131","111","472","513","462","405","304","515","324","420",
            "448","210","412","222","219","463","528","501","281","312","581","148","544","206","519","562","142","165","449","404","241",
            "536","264","234","233","146","101","156","105","332","310","138","314","112","123","511","508","235","122","319","330","135",
            "557","434","109","473","403","166","203","126","558","522","419","559","136","223","560","415","158","201","325","450","464",
            "322","564","117","116","700");

    private static final List<String> PAIS_OTORGANTE = Arrays.asList("AF", "AL", "AD", "AO", "AI", "AQ", "AG", "AN", "SA", "DZ", "AR", "AM", "AW", "AU", "AT", "AZ", "BS", "BH",
                                                                    "BD", "BB", "BY", "BE", "BZ", "BJ", "BM", "BT", "BO", "BA", "BW", "BR", "BN", "BG", "BF", "BI", "CV", "KH",
                                                                    "CM", "CA", "TD", "CL", "CN", "CY", "CO", "KM", "CG", "CI", "CR", "HR", "CU", "DK", "DJ", "DM", "EC", "EG",
                                                                    "SV", "AE", "ER", "SI", "ES", "VA", "US", "EE", "ET", "RU", "FJ", "PH", "FI", "FR", "FX", "GA", "GM", "GE", 
                                                                    "GS", "GH", "GI", "GD", "GR", "GL", "GP", "GU", "GT", "GF", "GQ", "GY", "HT", "HN", "HK", "HU", "IN", "ID",
                                                                    "IR", "IQ", "IE", "BV", "CX", "NU", "NF", "PN", "IS", "KY", "CC", "CK", "FO", "HM", "FK", "MP", "MH", "UM",
                                                                    "SB", "SJ", "TC", "VG", "VI", "WF", "IL", "IT", "LY", "JM", "JP", "JO", "KZ", "KE", "KG", "KI", "KW", "LS",
                                                                    "LV", "LB", "LR", "LI", "LT", "LU", "MO", "MG", "MY", "MW", "MV", "ML", "MT", "MA", "MQ", "MU", "MR", "YT",
                                                                    "MX", "FM", "MD", "MC", "MN", "MS", "MZ", "MM", "NA", "NR", "NP", "NI", "NE", "NG", "NO", "NC", "NZ", "OM",
                                                                    "NL", "PK", "PW", "PA", "PG", "PY", "PE", "PF", "PL", "PT", "PR", "QA", "GB", "SY", "CF", "CZ", "KR",
                                                                    "LA", "DO", "SK", "DE", "KP", "TZ", "RE", "RO", "RW", "EH", "KN", "WS", "AS", "SM", "PM", "VC", "SH", "LC",
                                                                    "ST", "SN", "SC", "SL", "SG", "SO", "LK", "ZA", "SD", "SE", "CH", "SR", "SZ", "TH", "TW", "TJ", "IO", "TF",
                                                                    "TP", "TG", "TK", "TO", "TT", "TN", "TM", "TR", "TV", "UA", "UG", "GN", "GW", "UY", "UZ", "VU", "VE", "VN",
                                                                    "YE", "RS", "ZM","ZW");
	
	private final String version;
	
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1862(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        version = contexto.getEsquema().getVersion();
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H315.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH315(final Registro registro) {
    	final long c6 = ((Number) registro.getPorCodigoSII("C6")).longValue();
    	return c6 > 0;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H317.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH317(final Registro registro) {
    	final String c9 = String.valueOf(registro.getPorCodigoSII("C9"));
    	return CODIGOS_OPERACIONES_CAMBIOS_INTERNACIONALES.contains(c9);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H318.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH318(final Registro registro) {
    	final String c8 = String.valueOf(registro.getPorCodigoSII("C8"));
    	return MODALIDAD_OPERACION.contains(c8);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H319.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH319(final Registro registro) {
    	final Date c10 = (Date) registro.getPorCodigoSII("C10");
    	try{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	    	//String anio = String.valueOf(Integer.parseInt(version)-1);
            String anio = String.valueOf(Integer.parseInt(version));
			final Date dateInicio = formatter.parse(anio + "-01-01");
			final Date dateFin = formatter.parse(anio + "-12-31");
			return (dateInicio.compareTo(c10) < 0 || dateInicio.compareTo(c10) == 0) && (dateFin.compareTo(c10) > 0 || dateFin.compareTo(c10) == 0);
		}catch(Exception e){
			return false;
		}
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H320.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH320(final Registro registro) {
    	final String c5 = String.valueOf(registro.getPorCodigoSII("C5"));
    	return MONEDA.contains(c5);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H321.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH321(final Registro registro) {
    	final String c13 = String.valueOf(registro.getPorCodigoSII("C13"));
    	return PAISES.contains(c13);
    }

    /* validación H754
    public boolean validarH754(final Registro registro){
        final String c15 = String.valueOf(registro.getPorCodigoSII("C15"));
        final String c0 = Strig.valueOf(registro.getPorCodigoSII("C0"));
        return true;
    }
    validación H754 */
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H831.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH831(final Registro registro) {
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	if(!c3.isEmpty()) return PAISES.contains(c3);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H832.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH832(final Registro registro) {
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	final String c2 = String.valueOf(registro.getPorCodigoSII("C2"));
    	if(!c2.isEmpty()) return !c3.isEmpty() && PAIS_OTORGANTE.contains(c3);
    	return true;
    }


    /* validación H854 
    public boolean validarH854(final Registro registro){
        final String c16 = String.valueOf(registro.getPorCodigoSII("C16"));
    }
    validación H854 */


    /* validación H855
    public boolean validarH855(final Registro registro){
        final String c17 = String.valueOf(registro.getPorCodigoSII("C17"));
    }
    validación H855 */
    
}