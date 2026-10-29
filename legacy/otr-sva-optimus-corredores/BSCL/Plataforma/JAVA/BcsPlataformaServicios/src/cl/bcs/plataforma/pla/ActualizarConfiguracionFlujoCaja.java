package cl.bcs.plataforma.pla;

/*
 * Clase generada automaticamente por Sp2Java
 */

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarConfiguracionFlujoCaja implements JavaCall {

  private final Log log = new Log("PLA.ActualizarConfiguracionFlujoCaja");

   /*
    * Una query y un batch (SELECT Y UPSERT)
    * La primera saca los datos de la vista "ConfiguracionFlujoCaja"
    * La segunda actualiza la tabla "ConfiguracionFlujoCaja"
   */
  private final String SQL_SELECT_CONFIGURACION = loadResourceToString(
      "cl/bcs/plataforma/pla/sql/ActualizarConfiguracionFlujoCaja_SelectConfiguracionFlujoCaja.sql");
  private final String SQL_INSERT_UPDATE_CONFIGURACION = loadResourceToString(
      "cl/bcs/plataforma/pla/sql/ActualizarConfiguracionFlujoCaja_UpsertConfiguracionFlujoCaja.sql");

  /*
   * Clase de entrada para recibir los parametros.
   * p_IdEmpresa                     :Long    ; id de la empresa. 
   * p_IdUsuario                     :Long    ; id del usuario.
   * p_IdModulo                      :String ; el modulo el cual se esta trabajando, en este caso "PLA"(Plataforma).
   * p_ConfiguracionFlujoCaja        :List<ConfiguracionFlujoCaja>; es un ArrayList de ConfiguracionFlujoCaja.
  */
  public static class ActualizarConfiguracionFlujoCaja_Entrada {

    public Long p_IdEmpresa;
    public Long p_IdUsuario;
    public String p_IdModulo;
    public List<ConfiguracionFlujoCaja> p_ConfiguracionFlujoCaja;

    public ActualizarConfiguracionFlujoCaja_Entrada() {
      p_ConfiguracionFlujoCaja = new ArrayList<>();
    }

  }
  /* 
   * Clase que instancia los objetos recibidos por la grilla "ConfiguracionFlujoCaja" de la interfaz web 
   * mediante la clase ActualizarConfiguracionFlujoCaja_Entrada que tiene un ArrayList de estos objetos:
   * IdUsoFlujoCaja      :Long;   identifica el NombreUso al cual pertenece el Item.
   * Item                :String; item de operacion ejemplo: "PRESTAMOS" con el cual se esta operando. 
   * FuncionOperador     :String; tres valores posibles: '0','1' o '+' que calcula si para cada "Item" del "IdUsoFlujoCaja"
   * su valor a operar.
   * 
 */
  public static class ConfiguracionFlujoCaja {
    public Long IdUsoFlujoCaja;
    public String Item;
    public String FuncionOperador;
  }

  public static class ActualizarConfiguracionFlujoCaja_Salida {

    public Long P_OUT_ERRORESCONTADOR;
    public List<MensajeSalida> P_OUT_MENSAJES;

    public ActualizarConfiguracionFlujoCaja_Salida() {
      P_OUT_MENSAJES = new ArrayList<>();
    }

  }

  @Override
  public JsonObject call(Connection conn, JsonObject jin) throws Exception {
    Gson gson = Utils.getGsonBuilder();
    ActualizarConfiguracionFlujoCaja_Entrada in =
        gson.fromJson(jin.toString(), ActualizarConfiguracionFlujoCaja_Entrada.class);
    ActualizarConfiguracionFlujoCaja_Salida out = logica(conn, in);
    out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
    JsonParser parser = new JsonParser();
    JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
    return dataout;
  }
  /*
   * Clase que instancia el objeto con el cual operare para realizar el pivoteo
   * y proceso de los datos recibidos, objeto que tambien listare para hacer el UPSERT definitivo.
   * IdUsoFlujoCaja            : Long  ; Id que matchea con cada Nombre Uso.
   * NombreUso                 : String; Nombre de no se que(?) Ej: "ORDENES_DE_COMPRA", "MESA_RF" etc...
   * IdEmpresa                 : Long  ; id de la empresa.
   * IdEstadoUso               : String; "ACT".
   * ItemCalculo               : String; descrito en la clase "ConfiguracionFlujoCaja" como "Item". 
   * ItemOrigen                : String; Origen del item (?).
   * DescripcionConfiguracion  : String; Descripcion del par Item Calculo-Item Origen que es unico para cada IdUsoFlujoCaja (o NombreUso).
   * FuncionOperador           : String; descrito en la clase "ConfiguracionFlujoCaja".
   * IdEstadoConfiguracion     : String; "ACT". 
   * NombreEstadoConfiguracion : String; Descripcion del estado del Item Ej: "GIRADO", "RECEPCIONADO", "ACTIVO" etc..
   * DesplegarEnUI             : String; Define si se va a desplegar en la UI de la plataforma web.
   * EsModificableUsuario      : String; Por default 'N' para valores que no existian en la tabla 'FlujoCaja' y se van a anadir.
   */
  public static class ConfiguracionFlujoCaja_Proceso {
    public Long IdUsoFlujoCaja;
    public String NombreUso;
    public Long IdEmpresa;
    public String IdEstadoUso;
    public String ItemCalculo;
    public String ItemOrigen;
    public String DescripcionConfiguracion;
    public String FuncionOperador;
    public String IdEstadoConfiguracion;
    public String NombreEstadoConfiguracion;
    public String DesplegarEnUI;
    public String EsModificableUsuario = "N";

    public String toString() {
      return Utils.printJson(this, false);
    }
  }

  // Logica de salida, aqui ocurre la actualizacion de los datos.
  private ActualizarConfiguracionFlujoCaja_Salida logica(Connection conn,
      ActualizarConfiguracionFlujoCaja_Entrada in) throws SQLException {

    ActualizarConfiguracionFlujoCaja_Salida salida = new ActualizarConfiguracionFlujoCaja_Salida();

    // SQL,
    SQL sql = new SQL();

    HashMap<String, Object> params = new HashMap<>();
    params.put("IdEmpresa", in.p_IdEmpresa);
    
    // Saca los datos originales de la vista "ConsultarConfiguracionFlujoCaja" y los guarda en "configuraciones".
    List<ConfiguracionFlujoCaja_Proceso> configuraciones =
        sql.query(conn, SQL_SELECT_CONFIGURACION, params, ConfiguracionFlujoCaja_Proceso.class);

    // Creacion de dos Hashmaps: "CalculoOrigen" y "IdUsoNombre",
    // El primero relaciona cada "ItemCalculo" con un ItemOrigen
    // y el segundo cada "IdUsoFlujoCaja" con un "NombreUso"
    HashMap<String, String> CalculoOrigen = new HashMap<>();
    HashMap<Long, String> IdUsoNombre = new HashMap<>();
    for (ConfiguracionFlujoCaja_Proceso o : configuraciones) {
      if (!(IdUsoNombre.containsKey(o.IdUsoFlujoCaja))) {
        IdUsoNombre.put(o.IdUsoFlujoCaja, o.NombreUso);
      }
      if (!(CalculoOrigen.containsKey(o.ItemCalculo))) {
        CalculoOrigen.put(o.ItemCalculo, o.ItemOrigen);
      }
    }
   
    // Aqui se ve si hay discrepancias de los datos originales de la vista "ConsultarConfiguracionFlujoCaja"
    // con los datos modificados que ingresaron como "in.p_ConfiguracionFlujoCaja" desde la grilla
    // en la interfaz web, se actualizaran los datos en "configuraciones" y los datos no existentes
    // se insertaran en lista.
    List<ConfiguracionFlujoCaja_Proceso> lista = new ArrayList<ConfiguracionFlujoCaja_Proceso>();
    long a = 0;
    for (int i = 0; i < in.p_ConfiguracionFlujoCaja.size(); i++) {
      a = 0;
      for (int j = 0; j < configuraciones.size(); j++) {
        if (configuraciones.get(j).IdUsoFlujoCaja
            .equals(in.p_ConfiguracionFlujoCaja.get(i).IdUsoFlujoCaja)
            && configuraciones.get(j).ItemCalculo.equals(in.p_ConfiguracionFlujoCaja.get(i).Item)) {
          ConfiguracionFlujoCaja_Proceso temp = new ConfiguracionFlujoCaja_Proceso();
          temp.IdUsoFlujoCaja = configuraciones.get(j).IdUsoFlujoCaja;
          temp.NombreUso = configuraciones.get(j).NombreUso;
          temp.IdEmpresa = configuraciones.get(j).IdEmpresa;
          temp.IdEstadoUso = configuraciones.get(j).IdEstadoUso;
          temp.ItemCalculo = configuraciones.get(j).ItemCalculo;
          temp.ItemOrigen = configuraciones.get(j).ItemOrigen;
          temp.DescripcionConfiguracion = configuraciones.get(j).DescripcionConfiguracion;
          temp.FuncionOperador = in.p_ConfiguracionFlujoCaja.get(i).FuncionOperador;
          temp.IdEstadoConfiguracion = configuraciones.get(j).IdEstadoConfiguracion;
          temp.NombreEstadoConfiguracion = configuraciones.get(j).NombreEstadoConfiguracion;
          temp.DesplegarEnUI = configuraciones.get(j).DesplegarEnUI;
          configuraciones.set(j, temp);
          a = 1;
        }
        if (a == 0 && j == (configuraciones.size()) - 1) {
          ConfiguracionFlujoCaja_Proceso temp = new ConfiguracionFlujoCaja_Proceso();
          temp.IdUsoFlujoCaja = in.p_ConfiguracionFlujoCaja.get(i).IdUsoFlujoCaja;
          temp.NombreUso = IdUsoNombre.get(in.p_ConfiguracionFlujoCaja.get(i).IdUsoFlujoCaja);
          temp.IdEmpresa = in.p_IdEmpresa;
          temp.IdEstadoUso = "ACT";
          temp.ItemCalculo = in.p_ConfiguracionFlujoCaja.get(i).Item;
          temp.ItemOrigen = CalculoOrigen.get(in.p_ConfiguracionFlujoCaja.get(i).Item);
          temp.DescripcionConfiguracion = "";
          temp.FuncionOperador = in.p_ConfiguracionFlujoCaja.get(i).FuncionOperador;
          temp.IdEstadoConfiguracion = "ACT";
          temp.NombreEstadoConfiguracion = "ACTIVO";
          temp.DesplegarEnUI = "S";
          lista.add(temp);
        }
      }
    }
    //"TODOMEN" es un ArrayList<ConfiguracionFlujoCaja_Proceso>, con esto se actualiza la tabla "ConfiguracionFlujoCaja"
    // se anaden en "TODOMEN" todos los datos para luego enviarlos a la tabla "ConfiguracionFlujoCaja".
    List<ConfiguracionFlujoCaja_Proceso> TODOMEN = new ArrayList<ConfiguracionFlujoCaja_Proceso>();
    TODOMEN.addAll(configuraciones);
    TODOMEN.addAll(lista);
    
    // Actualiza la tabla FlujoCaja con un UPSERT, usando "TODOMEN".
    sql.batch(conn, SQL_INSERT_UPDATE_CONFIGURACION, TODOMEN, ConfiguracionFlujoCaja_Proceso.class);

    return salida;
  }


  // TEST DE PRUEBA, EXCLUSIVAMENTE PARA PROBAR.
  public static void main(String[] args) throws Exception {
    System.out.println("== TEST ==");
    try (Connection con = PlataformaUtils.getDESAConnection()) {
      Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues()
          .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
      String json =
          "{\"p_IdEmpresa\":1,\"p_IdUsuario\":37,\"p_IdModulo\":\"PLA\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_ConfiguracionFlujoCaja\":[{\"Item\":\"MOVIMIENTOS_FACTURAR_COMPRAS\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"-\"},{\"Item\":\"MOVIMIENTOS_FACTURAR_COMPRAS\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"0\"},{\"Item\":\"MOVIMIENTOS_FACTURAR_COMPRAS\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"MOVIMIENTOS_FACTURAR_COMPRAS\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"MOVIMIENTOS_FACTURAR_COMPRAS\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"MOVIMIENTOS_FACTURAR_COMPRAS\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"-\"},{\"Item\":\"ORDENES_EN_TRANSITO_COMPRAS\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"-\"},{\"Item\":\"ORDENES_EN_TRANSITO_COMPRAS\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"-\"},{\"Item\":\"ORDENES_EN_TRANSITO_COMPRAS\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"ORDENES_EN_TRANSITO_COMPRAS\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"-\"},{\"Item\":\"ORDENES_EN_TRANSITO_COMPRAS\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"ORDENES_EN_TRANSITO_COMPRAS\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"-\"},{\"Item\":\"ASIGNACIONES_COMPRAS\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"-\"},{\"Item\":\"ASIGNACIONES_COMPRAS\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"-\"},{\"Item\":\"ASIGNACIONES_COMPRAS\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"ASIGNACIONES_COMPRAS\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"ASIGNACIONES_COMPRAS\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"ASIGNACIONES_COMPRAS\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"-\"},{\"Item\":\"APORTES_EN_TRANSITO\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"-\"},{\"Item\":\"APORTES_EN_TRANSITO\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"-\"},{\"Item\":\"APORTES_EN_TRANSITO\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"-\"},{\"Item\":\"APORTES_EN_TRANSITO\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"APORTES_EN_TRANSITO\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"APORTES_EN_TRANSITO\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"+\"},{\"Item\":\"INGRESOS_TESORERIA\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"-\"},{\"Item\":\"INGRESOS_TESORERIA\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"+\"},{\"Item\":\"INGRESOS_TESORERIA\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"INGRESOS_TESORERIA\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"INGRESOS_TESORERIA\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"INGRESOS_TESORERIA\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"+\"},{\"Item\":\"SALDO_CUENTA_INVERSION\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"+\"},{\"Item\":\"SALDO_CUENTA_INVERSION\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"+\"},{\"Item\":\"SALDO_CUENTA_INVERSION\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"+\"},{\"Item\":\"SALDO_CUENTA_INVERSION\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"+\"},{\"Item\":\"SALDO_CUENTA_INVERSION\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"+\"},{\"Item\":\"SALDO_CUENTA_INVERSION\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"+\"},{\"Item\":\"COMPRA_PACTO\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"-\"},{\"Item\":\"COMPRA_PACTO\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"0\"},{\"Item\":\"COMPRA_PACTO\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"COMPRA_PACTO\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"COMPRA_PACTO\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"+\"},{\"Item\":\"COMPRA_PACTO\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"-\"},{\"Item\":\"COMPRA_TERMINO\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"-\"},{\"Item\":\"COMPRA_TERMINO\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"+\"},{\"Item\":\"COMPRA_TERMINO\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"COMPRA_TERMINO\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"COMPRA_TERMINO\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"+\"},{\"Item\":\"COMPRA_TERMINO\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"-\"},{\"Item\":\"ORDENES_EN_TRANSITO_VENTAS\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"0\"},{\"Item\":\"ORDENES_EN_TRANSITO_VENTAS\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"+\"},{\"Item\":\"ORDENES_EN_TRANSITO_VENTAS\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"ORDENES_EN_TRANSITO_VENTAS\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"+\"},{\"Item\":\"ORDENES_EN_TRANSITO_VENTAS\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"ORDENES_EN_TRANSITO_VENTAS\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"+\"},{\"Item\":\"ASIGNACIONES_VENTAS\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"0\"},{\"Item\":\"ASIGNACIONES_VENTAS\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"+\"},{\"Item\":\"ASIGNACIONES_VENTAS\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"ASIGNACIONES_VENTAS\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"ASIGNACIONES_VENTAS\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"ASIGNACIONES_VENTAS\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"+\"},{\"Item\":\"APORTES_EN_TRANSITO_INSTRUCCION_LIQUIDADA\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"0\"},{\"Item\":\"APORTES_EN_TRANSITO_INSTRUCCION_LIQUIDADA\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"-\"},{\"Item\":\"APORTES_EN_TRANSITO_INSTRUCCION_LIQUIDADA\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"APORTES_EN_TRANSITO_INSTRUCCION_LIQUIDADA\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"APORTES_EN_TRANSITO_INSTRUCCION_LIQUIDADA\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"APORTES_EN_TRANSITO_INSTRUCCION_LIQUIDADA\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"0\"},{\"Item\":\"RESCATES_EN_TRANSITO\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"0\"},{\"Item\":\"RESCATES_EN_TRANSITO\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"+\"},{\"Item\":\"RESCATES_EN_TRANSITO\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"-\"},{\"Item\":\"RESCATES_EN_TRANSITO\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"RESCATES_EN_TRANSITO\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"RESCATES_EN_TRANSITO\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"-\"},{\"Item\":\"EGRESOS_TESORERIA\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"0\"},{\"Item\":\"EGRESOS_TESORERIA\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"-\"},{\"Item\":\"EGRESOS_TESORERIA\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"EGRESOS_TESORERIA\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"EGRESOS_TESORERIA\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"EGRESOS_TESORERIA\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"+\"},{\"Item\":\"INVERSION_EN_LIQUIDEZ\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"0\"},{\"Item\":\"INVERSION_EN_LIQUIDEZ\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"+\"},{\"Item\":\"INVERSION_EN_LIQUIDEZ\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"INVERSION_EN_LIQUIDEZ\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"INVERSION_EN_LIQUIDEZ\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"INVERSION_EN_LIQUIDEZ\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"0\"},{\"Item\":\"VENTA_PACTO\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"0\"},{\"Item\":\"VENTA_PACTO\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"0\"},{\"Item\":\"VENTA_PACTO\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"VENTA_PACTO\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"VENTA_PACTO\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"-\"},{\"Item\":\"VENTA_PACTO\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"+\"},{\"Item\":\"VENTA_TERMINO\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"0\"},{\"Item\":\"VENTA_TERMINO\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"0\"},{\"Item\":\"VENTA_TERMINO\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"VENTA_TERMINO\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"VENTA_TERMINO\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"-\"},{\"Item\":\"VENTA_TERMINO\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"+\"},{\"Item\":\"DIVIDENDOS_EN_DINERO\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"0\"},{\"Item\":\"DIVIDENDOS_EN_DINERO\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"0\"},{\"Item\":\"DIVIDENDOS_EN_DINERO\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"DIVIDENDOS_EN_DINERO\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"DIVIDENDOS_EN_DINERO\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"DIVIDENDOS_EN_DINERO\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"+\"},{\"Item\":\"CORTE_CUPON\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"0\"},{\"Item\":\"CORTE_CUPON\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"0\"},{\"Item\":\"CORTE_CUPON\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"CORTE_CUPON\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"CORTE_CUPON\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"CORTE_CUPON\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"+\"},{\"Item\":\"SORTEO_DE_LETRAS\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"0\"},{\"Item\":\"SORTEO_DE_LETRAS\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"0\"},{\"Item\":\"SORTEO_DE_LETRAS\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"SORTEO_DE_LETRAS\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"SORTEO_DE_LETRAS\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"SORTEO_DE_LETRAS\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"+\"},{\"Item\":\"MOVIMIENTOS_FACTURAR_VENTAS\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"0\"},{\"Item\":\"MOVIMIENTOS_FACTURAR_VENTAS\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"0\"},{\"Item\":\"MOVIMIENTOS_FACTURAR_VENTAS\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"MOVIMIENTOS_FACTURAR_VENTAS\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"MOVIMIENTOS_FACTURAR_VENTAS\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"MOVIMIENTOS_FACTURAR_VENTAS\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"+\"},{\"Item\":\"SIMULTANEA_COMPRAS\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"-\"},{\"Item\":\"SIMULTANEA_COMPRAS\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"0\"},{\"Item\":\"SIMULTANEA_COMPRAS\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"SIMULTANEA_COMPRAS\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"SIMULTANEA_COMPRAS\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"SIMULTANEA_COMPRAS\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"-\"},{\"Item\":\"SIMULTANEA_VENTAS\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"0\"},{\"Item\":\"SIMULTANEA_VENTAS\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"0\"},{\"Item\":\"SIMULTANEA_VENTAS\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"SIMULTANEA_VENTAS\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"SIMULTANEA_VENTAS\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"SIMULTANEA_VENTAS\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"+\"},{\"Item\":\"EMPRESTITOS\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"0\"},{\"Item\":\"EMPRESTITOS\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"0\"},{\"Item\":\"EMPRESTITOS\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"EMPRESTITOS\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"EMPRESTITOS\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"EMPRESTITOS\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"+\"},{\"Item\":\"PRESTAMOS\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"0\"},{\"Item\":\"PRESTAMOS\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"0\"},{\"Item\":\"PRESTAMOS\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"PRESTAMOS\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"PRESTAMOS\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"PRESTAMOS\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"+\"},{\"Item\":\"CARGOS_PENDIENTES_CUENTA_INVERSION\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"0\"},{\"Item\":\"CARGOS_PENDIENTES_CUENTA_INVERSION\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"0\"},{\"Item\":\"CARGOS_PENDIENTES_CUENTA_INVERSION\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"CARGOS_PENDIENTES_CUENTA_INVERSION\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"CARGOS_PENDIENTES_CUENTA_INVERSION\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"CARGOS_PENDIENTES_CUENTA_INVERSION\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"+\"},{\"Item\":\"COMPRA_FORWARD\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"0\"},{\"Item\":\"COMPRA_FORWARD\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"0\"},{\"Item\":\"COMPRA_FORWARD\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"0\"},{\"Item\":\"COMPRA_FORWARD\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"0\"},{\"Item\":\"COMPRA_FORWARD\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"0\"},{\"Item\":\"COMPRA_FORWARD\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"-\"},{\"Item\":\"VENTA_FORWARD\",\"IdUsoFlujoCaja\":1,\"FuncionOperador\":\"-\"},{\"Item\":\"VENTA_FORWARD\",\"IdUsoFlujoCaja\":3,\"FuncionOperador\":\"-\"},{\"Item\":\"VENTA_FORWARD\",\"IdUsoFlujoCaja\":4,\"FuncionOperador\":\"+\"},{\"Item\":\"VENTA_FORWARD\",\"IdUsoFlujoCaja\":5,\"FuncionOperador\":\"-\"},{\"Item\":\"VENTA_FORWARD\",\"IdUsoFlujoCaja\":6,\"FuncionOperador\":\"+\"},{\"Item\":\"VENTA_FORWARD\",\"IdUsoFlujoCaja\":100000,\"FuncionOperador\":\"+\"}]}";
      ActualizarConfiguracionFlujoCaja_Entrada Objin =
          gson.fromJson(json, ActualizarConfiguracionFlujoCaja_Entrada.class);
      ActualizarConfiguracionFlujoCaja demo = new ActualizarConfiguracionFlujoCaja();
      try {
        ActualizarConfiguracionFlujoCaja_Salida ObjOut = demo.logica(con, Objin);
        con.commit();
      }

      catch (Exception er) {
        er.printStackTrace();
        con.rollback();
      }
      // con.commit();

    }

    System.out.println("== END ==");
  }

}
