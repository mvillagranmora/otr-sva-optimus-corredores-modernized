set schema bscl;

/*****************************************************************************************************************
.______    __          ___   .___________.    ___       _______   ______   .______      .___  ___.      ___      
|   _  \  |  |        /   \  |           |   /   \     |   ____| /  __  \  |   _  \     |   \/   |     /   \     
|  |_)  | |  |       /  ^  \ `---|  |----`  /  ^  \    |  |__   |  |  |  | |  |_)  |    |  \  /  |    /  ^  \    
|   ___/  |  |      /  /_\  \    |  |      /  /_\  \   |   __|  |  |  |  | |      /     |  |\/|  |   /  /_\  \   
|  |      |  `----./  _____  \   |  |     /  _____  \  |  |     |  `--'  | |  |\  \----.|  |  |  |  /  _____  \  
| _|      |_______/__/     \__\  |__|    /__/     \__\ |__|      \______/  | _| `._____||__|  |__| /__/     \__\ 
                                                                                                                 
******************************************************************************************************************/
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where  "CodLenguaje" = 'PE';


-- BORRAR:
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_ACTIVACION_MASIVA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_BLOQUEO_AUTOMATICO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_BLOQUEO_AUTOMATICO_CLIENTES' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_BLOQUEO_AUTOMATICO_VENCIMIENTO_DOCUMENTOS' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_AÑOS' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_ANOS' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_MESES' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_BLOQUEO_CUMPLIR_MAYORIA_EDAD' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_BLOQUEO_FICHA_DESACTUALIZADA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_BLOQUEO_PORTAFOLIO_SIN_OPERACIONES' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_APERTURA_MODULO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_GESTION_FIX' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_ENVIO_FACTURACION' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_CONSULTA_AVANZADA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_CONSULTA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_ACTIVACION_MASIVA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_BLOQUEO_AUTOMATICO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_BLOQUEO_AUTOMATICO_CLIENTES' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_JUNTA_ACCIONISTAS' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_SUSPENDER_TEMPORAL' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_FECHA_INICIO_SUSPENSION' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_FECHA_TERMINO_SUSPENSION' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_SUCURSAL_PREFERENCIA_CLIENTE' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_PARTICIPACION_MAYOR' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_REFERENCIA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_TIPO_BANCO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_IDENTIFICADOR_BANCO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_NACIONALIDAD_SECUNDARIA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_GRADO_INSTRUCCION' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_GIRO_CIIU' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_RESIDENCIA_SECUNDARIA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_DIRECCION_SECUNDARIA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_ANOS_SERVICIO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_INGRESO_PROMEDIO_MENSUAL' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_ACTIVACION_MASIVA_CLIENTES' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_INFORMACION_INTERMEDIARIO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_RANGO_PATRIMONIO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_GRADO_VINCULACION' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_CONDICIONES' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_CLASIFICACION_EMPRESA' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_NUMERO_ADICIONAL' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_NUMERO_IDENTIDAD' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_MODALIDAD_DE_ORDENES' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_ENVIO_FACTURA_POR_FISICO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_NUMERO_REGISTRO_PUBLICO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_ASIENTO' and "CodLenguaje"='CL';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" = 'UI_FICHA_O_PARTIDA' and "CodLenguaje"='CL';

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ACTIVACION_MASIVA','CL','Activación masiva','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_BLOQUEO_AUTOMATICO','CL','Bloqueo automático','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_BLOQUEO_AUTOMATICO_CLIENTES','CL','Bloqueo automático de clientes','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_BLOQUEO_AUTOMATICO_VENCIMIENTO_DOCUMENTOS','CL','Bloqueo automático por vencimiento de documentos','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_AÑOS','CL','años','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ANOS','CL','años','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MESES','CL','meses','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_BLOQUEO_CUMPLIR_MAYORIA_EDAD','CL','Bloqueo tras cumplir mayoría de edad','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_BLOQUEO_FICHA_DESACTUALIZADA','CL','Bloqueo ficha desactualizada','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_BLOQUEO_PORTAFOLIO_SIN_OPERACIONES','CL','Bloqueo portafolio sin operaciones','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_APERTURA_MODULO','CL','Apertura de módulo','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_GESTION_FIX','CL','Gestión FIX','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ENVIO_FACTURACION','CL','Envío Facturación','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CONSULTA_AVANZADA','CL','Consulta avanzada','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CONSULTA','CL','Consulta','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ACTIVACION_MASIVA','CL','Activación masiva','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_BLOQUEO_AUTOMATICO','CL','Bloqueo automático','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_BLOQUEO_AUTOMATICO_CLIENTES','CL','Bloqueo automático de clientes','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_JUNTA_ACCIONISTAS','CL','Junta de accionistas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_SUSPENDER_TEMPORAL','CL','Suspender temporalmente','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_FECHA_INICIO_SUSPENSION','CL','Inicio suspensión','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_FECHA_TERMINO_SUSPENSION','CL','Término suspensión','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_SUCURSAL_PREFERENCIA_CLIENTE','CL','Sucursal preferencia cliente','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PARTICIPACION_MAYOR','CL','Participación mayor','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_REFERENCIA','CL','Referencia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_TIPO_BANCO','CL','Tipo de banco','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_IDENTIFICADOR_BANCO','CL','Identificador de banco','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_NACIONALIDAD_SECUNDARIA','CL','Nacionalidad secundaria','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_GRADO_INSTRUCCION','CL','Grado de instrucción','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_GIRO_CIIU','CL','Giro CIIU','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_RESIDENCIA_SECUNDARIA','CL','Residencia secundaria','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIRECCION_SECUNDARIA','CL','Dirección secundaria','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ANOS_SERVICIO','CL','Años de servicio','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_INGRESO_PROMEDIO_MENSUAL','CL','Ingreso promedio mensual','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ACTIVACION_MASIVA_CLIENTES','CL','Activación masiva de clientes','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_INFORMACION_INTERMEDIARIO','CL','Información intermediario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_RANGO_PATRIMONIO','CL','Rango patrimonio','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_GRADO_VINCULACION','CL','Grado de vinculación','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CONDICIONES','CL','Condiciones','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CLASIFICACION_EMPRESA','CL','Clasificación empresa','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_NUMERO_ADICIONAL','CL','Número adicional','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_NUMERO_IDENTIDAD','CL','Número de entidad','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MODALIDAD_DE_ORDENES','CL','Modalidad de órdenes','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ENVIO_FACTURA_POR_FISICO','CL','Envío factura por físico','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_NUMERO_REGISTRO_PUBLICO','CL','Número de registro público','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ASIENTO','CL','Asiento','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_FICHA_O_PARTIDA','CL','Ficha o partida','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MOTIVO_ACTIVACION_CLIENTE','CL','Motivo activación cliente','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MOTIVO_ACTIVACION_PORTAFOLIO','CL','Motivo activación portafolio','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ACTIVAR','CL','Activar','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_NUMERO_SECUNDARIO','CL','Número Secundario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_NUMERO_IDENTIDAD','CL','Número Identidad','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_UBIGEO','CL','Ubigeo','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CODIGO_COMPUESTO_DIRECCION_FISICA','CL','Ubigeo','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CODIGO_ADICIONAL_HOLDING','CL','Código adicional Holding','');

insert into "BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario","CodLenguaje","Descripcion","Abreviado")
select "CodDiccionario",'PE',"Descripcion","Abreviado"  from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodLenguaje" = 'CL';

update "BSCL.Plataforma.BD::PLA.Diccionario" set "Descripcion" = replace("Descripcion",'Tipo Cliente','Clase Cliente') 
where "CodLenguaje" = 'PE' and  "Descripcion" like '%Tipo Cliente%';

update "BSCL.Plataforma.BD::PLA.Diccionario" set "Descripcion" = replace("Descripcion",'Agente','Asesor Comercial') 
where "CodLenguaje" = 'PE' and  "Descripcion" like '%Agente%';

commit;
-- actualizar diccionario: UI_NACIONALIDAD_PRINCIPAL = 'NACIONALIDAD PRINCIPAL'

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CLASE_CLIENTE','PE','Clase Cliente','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CREAR_CONYUGE','PE','Crear cónyuge','');
commit;

--BARRIDO DEL DICCIONARIO PARA CAMBIAR AGENTE POR ASESOR COMERCIAL
update "BSCL.Plataforma.BD::PLA.Diccionario" set "Descripcion" = replace("Descripcion",'Agente','Asesor Comercial') 
where "CodLenguaje" = 'PE' and  "Descripcion" like '%Agente%';
commit;

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_GARANTIAS','CL','PPP Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_PPP_GARANTIAS','CL','Monto PPP Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_GARANTIAS','CL','Precio Mercado Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_MERCADO_GARANTIAS','CL','Monto Mercado Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_GARANTIAS','CL','Diferencia Mercado Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_SIMULTANEA_COMPRAS','CL','PPP Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_PPP_SIMULTANEA_COMPRAS','CL','Monto PPP Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_SIMULTANEA_COMPRAS','CL','Precio Mercado Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_MERCADO_SIMULTANEA_COMPRAS','CL','Monto Mercado Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_SIMULTANEA_COMPRAS','CL','Diferencia Mercado Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_SIMULTANEA_VENTAS','CL','PPP Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_PPP_SIMULTANEA_VENTAS','CL','Monto PPP Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_SIMULTANEA_VENTAS','CL','Precio Mercado Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_MERCADO_SIMULTANEA_VENTAS','CL','Monto Mercado Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_SIMULTANEA_VENTAS','CL','Diferencia Mercado Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_PRESTATARIO','CL','PPP Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_PPP_PRESTATARIO','CL','Monto PPP Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_PRESTATARIO','CL','Precio Mercado Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_MERCADO_PRESTATARIO','CL','Monto Mercado Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_PRESTATARIO','CL','Diferencia Mercado Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_EMPRESTITO','CL','PPP Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_PPP_EMPRESTITO','CL','Monto PPP Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_EMPRESTITO','CL','Precio Mercado Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_MERCADO_EMPRESTITO','CL','Monto Mercado Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_EMPRESTITO','CL','Diferencia Mercado Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_CUSTODIA','CL','PPP Custodia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_PPP_CUSTODIA','CL','Monto PPP Custodia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_CUSTODIA','CL','Precio Mercado Custodia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_MERCADO_CUSTODIA','CL','Monto Mercado Custodia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_CUSTODIA','CL','Diferencia Mercado Custodia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_GARANTIAS','CL','PPP Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_PPP_GARANTIAS','CL','Monto PPP Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_GARANTIAS','CL','Precio Mercado Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_MERCADO_GARANTIAS','CL','Monto Mercado Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_GARANTIAS','CL','Diferencia Mercado Garantías','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_SIMULTANEA_COMPRAS','CL','PPP Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_PPP_SIMULTANEA_COMPRAS','CL','Monto PPP Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_SIMULTANEA_COMPRAS','CL','Precio Mercado Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_MERCADO_SIMULTANEA_COMPRAS','CL','Monto Mercado Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_SIMULTANEA_COMPRAS','CL','Diferencia Mercado Simultánea Compras','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_SIMULTANEA_VENTAS','CL','PPP Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_PPP_SIMULTANEA_VENTAS','CL','Monto PPP Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_SIMULTANEA_VENTAS','CL','Precio Mercado Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_MERCADO_SIMULTANEA_VENTAS','CL','Monto Mercado Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_SIMULTANEA_VENTAS','CL','Diferencia Mercado Simultánea Ventas','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_PRESTATARIO','CL','PPP Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_PPP_PRESTATARIO','CL','Monto PPP Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_PRESTATARIO','CL','Precio Mercado Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_MERCADO_PRESTATARIO','CL','Monto Mercado Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_PRESTATARIO','CL','Diferencia Mercado Prestatario','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_EMPRESTITO','CL','PPP Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_PPP_EMPRESTITO','CL','Monto PPP Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_EMPRESTITO','CL','Precio Mercado Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_MERCADO_EMPRESTITO','CL','Monto Mercado Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_EMPRESTITO','CL','Diferencia Mercado Prestamista','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PPP_CUSTODIA','CL','PPP Custodia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_PPP_CUSTODIA','CL','Monto PPP Custodia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_MERCADO_CUSTODIA','CL','Precio Mercado Custodia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_MERCADO_CUSTODIA','CL','Monto Mercado Custodia','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DIFERENCIA_MERCADO_CUSTODIA','CL','Diferencia Mercado Custodia','');

commit;
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" like 'UI_VALOR_MERCADO_%' or "CodDiccionario"  like 'UI_VALOR_PPP_%';
delete from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" like 'UI_MONTO_MERCADO_%' or "CodDiccionario"  like 'UI_MONTO_PPP_%';
commit;

select * from "BSCL.Plataforma.BD::PLA.Diccionario" where "CodDiccionario" like 'UI_VALOR_MERCADO_%' or "CodDiccionario"  like 'UI_VALOR_PPP_%';


/************** DICCIONARIO ***********/
/*
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_TIR_MEDIA','CL','TIR Media','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_TIR_EMISION','CL','TIR Emisión','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CLASIF_RIESGO','CL','Clasif.Riesgo','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_DURACION','CL','Duración','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CONVEXIDAD','CL','Convexidad','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MECANISMO_DE_CAPTACION','CL','Mecanismo de captación','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_NOMINAL','CL','Valor Nominal','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_INTERESES_CORRIDOS','CL','Intereses corridos','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_REAJUSTES_VAC_CORRIDO','CL','Reajustes/VAC corrido','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_MONTO_TOTAL_SIN_COMISIONES','CL','Monto total sin comisiones','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_TIR','CL','TIR','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_INGRESAR_ORDEN_RF','CL','Ingresar Orden Renta Fija','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_TIENE_CALL','CL','Valor tiene call','');
*/

-------------------------
-- VALORIZAR CARTERA
-------------------------
set schema bscl;
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_VALORIZADO','CL','Valor Valorizado','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_ACTUALIZADO','CL','Valor Actualizado','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_SUCIO','CL','Precio Sucio','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_TIR_NETA','CL','TIR Neta','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CLASE_DE_ORDEN','CL','Clase de Orden','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ES_ORDEN_ESPECIAL','CL','Es Orden Especial','');
	
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_VALORIZADO','PE','Valor Valorizado','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALOR_ACTUALIZADO','PE','Valor Actualizado','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_PRECIO_SUCIO','PE','Precio Sucio','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_TIR_NETA','PE','TIR Neta','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CLASE_DE_ORDEN','PE','Clase de Orden','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_ES_ORDEN_ESPECIAL','PE','Es Orden Especial','');
commit;

/***************************
 DETALLE COBRO PERSONALIZADO
 ***************************/
set schema bscl;

	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CALCULAR','CL','Calcular','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALORIZAR_TIR','CL','Valorizar TIR','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALORIZAR_PRECIO','CL','Valorizar Precio','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_CALCULAR','PE','Calcular','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALORIZAR_TIR','PE','Valorizar TIR','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )  VALUES('UI_VALORIZAR_PRECIO','PE','Valorizar Precio','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" ) VALUES('AUTORIZACION_ENROLAMIENTO_PERSONA_PENDIENTE','CL','Autorización de enrolamiento Persona Pendiente','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" ) VALUES('AUTORIZACION_ENROLAMIENTO_CLIENTE_PENDIENTE','CL','Autorización de enrolamiento Cliente Pendiente','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" ) VALUES('UI_AUTORIZACION_ENROLAMIENTO_PERSONA_PENDIENTE','CL','Autorización de enrolamiento Persona Pendiente','');
	INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" ) VALUES('UI_AUTORIZACION_ENROLAMIENTO_CLIENTE_PENDIENTE','CL','Autorización de enrolamiento Cliente Pendiente','');

commit;