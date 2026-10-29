SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
DELETE FROM  "BSCL.Plataforma.BD::MRE.ReglaOperacion"; 
DELETE FROM  "BSCL.Plataforma.BD::MRE.ReglaGrupo"; 
DELETE FROM  "BSCL.Plataforma.BD::MRE.ReglaEjecucion"; 
DELETE FROM  "BSCL.Plataforma.BD::MRE.Regla"; 
DELETE FROM  "BSCL.Plataforma.BD::MRE.ReglaSalida"; 
/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

INSERT INTO "BSCL.Plataforma.BD::MRE.ReglaOperacion" ( "IdReglaOperacion","IdEmpresa","IdGrupoRegla","Prioridad","DetieneValidacion","CodigoRegla","Ambito","Habilitado","CodigoComportamiento" )  
VALUES(1,0,0,1000,'N','TES001','R','S','ADVE');
INSERT INTO "BSCL.Plataforma.BD::MRE.ReglaOperacion" ( "IdReglaOperacion","IdEmpresa","IdGrupoRegla","Prioridad","DetieneValidacion","CodigoRegla","Ambito","Habilitado","CodigoComportamiento" )  
VALUES(2,1,1,1000,'N','FACCONCEPTOMOV','V','S','ADVE');
INSERT INTO "BSCL.Plataforma.BD::MRE.ReglaOperacion" ( "IdReglaOperacion","IdEmpresa","IdGrupoRegla","Prioridad","DetieneValidacion","CodigoRegla","Ambito","Habilitado","CodigoComportamiento" )  
VALUES(3,1,4,1000,'S','SALDOCTAINVMAYORACERO','V','S','SDET');
INSERT INTO "BSCL.Plataforma.BD::MRE.ReglaOperacion" ( "IdReglaOperacion","IdEmpresa","IdGrupoRegla","Prioridad","DetieneValidacion","CodigoRegla","Ambito","Habilitado","CodigoComportamiento" )  
VALUES(4,1,6,1000,'S','MOVIMIENTOCUMPLEREQUISITOANULACION','R','S','AUTH');
INSERT INTO "BSCL.Plataforma.BD::MRE.ReglaOperacion" ( "IdReglaOperacion","IdEmpresa","IdGrupoRegla","Prioridad","DetieneValidacion","CodigoRegla","Ambito","Habilitado","CodigoComportamiento" )  
VALUES(9,1,7,1000,'S','MONTOPERMITIDO','R','S','SDET');

INSERT INTO "BSCL.Plataforma.BD::MRE.ReglaGrupo" ( "IdGrupoRegla","IdEmpresa","Modulo","Descripcion","ProcedimientoCompilado","FechaCompilado" )  
VALUES(0,0,'MODULO DEMO','"PRUEBAS DE REGLAS"','X','0001-01-01 00:00:00.000000000');
INSERT INTO "BSCL.Plataforma.BD::MRE.ReglaGrupo" ( "IdGrupoRegla","IdEmpresa","Modulo","Descripcion","ProcedimientoCompilado","FechaCompilado" )  
VALUES(1,1,'FACTURACION','"CREAR MOVIMIENTO RV"','X','0001-01-01 00:00:00.000000000');
INSERT INTO "BSCL.Plataforma.BD::MRE.ReglaGrupo" ( "IdGrupoRegla","IdEmpresa","Modulo","Descripcion","ProcedimientoCompilado","FechaCompilado" )  
VALUES(2,1,'ORDENES','"COMPRA ACCIONES RV"','X','0001-01-01 00:00:00.000000000');
INSERT INTO "BSCL.Plataforma.BD::MRE.ReglaGrupo" ( "IdGrupoRegla","IdEmpresa","Modulo","Descripcion","ProcedimientoCompilado","FechaCompilado" )  
VALUES(3,1,'CUENTAINVERSION','"CREAR CUENTA INVERSION"','X','0001-01-01 00:00:00.000000000');
INSERT INTO "BSCL.Plataforma.BD::MRE.ReglaGrupo" ( "IdGrupoRegla","IdEmpresa","Modulo","Descripcion","ProcedimientoCompilado","FechaCompilado" )  
VALUES(4,1,'CUENTAINVERSION','"CREAR CARGO ABONO"','X','0001-01-01 00:00:00.000000000');
INSERT INTO "BSCL.Plataforma.BD::MRE.ReglaGrupo" ( "IdGrupoRegla","IdEmpresa","Modulo","Descripcion","ProcedimientoCompilado","FechaCompilado" )  
VALUES(5,1,'CUENTAINVERSION','"CREAR TRANSFERENCIA"','X','0001-01-01 00:00:00.000000000');
INSERT INTO "BSCL.Plataforma.BD::MRE.ReglaGrupo" ( "IdGrupoRegla","IdEmpresa","Modulo","Descripcion","ProcedimientoCompilado","FechaCompilado" )  
VALUES(6,1,'CUENTAINVERSION','"ANULAR CARGO ABONO"','X','0001-01-01 00:00:00.000000000');
INSERT INTO "BSCL.Plataforma.BD::MRE.ReglaGrupo" ( "IdGrupoRegla","IdEmpresa","Modulo","Descripcion","ProcedimientoCompilado","FechaCompilado" )  
VALUES(7,1,'TRIBUTARIO','"CREA REGISTRO OPERACIONES"','X','0001-01-01 00:00:00.000000000');

INSERT INTO "BSCL.Plataforma.BD::MRE.Regla" ( "CodigoRegla","Modulo","Descripcion","EsquemaDB","FuncionSP","Parametros" )  
VALUES('FACCONCEPTOMOV','FACTURACION','Validar la existencia del concepto usado para el ingreso de movimiento en facturacion','BSCL','BSCL.Plataforma.BD.Reglas::ConceptoExiste',' 'p_idconcepto','p_idmodulo'');
INSERT INTO "BSCL.Plataforma.BD::MRE.Regla" ( "CodigoRegla","Modulo","Descripcion","EsquemaDB","FuncionSP","Parametros" )  
VALUES('SALDOCTAINVMAYORACERO','CUENTAINVERSION','Regla para validar si el monto de un movimiento de cargo o abono genera un saldo menor a cero','BSCL','BSCL.CuentaInversion.BD.Reglas::SaldoMayorACero',' ');
INSERT INTO "BSCL.Plataforma.BD::MRE.Regla" ( "CodigoRegla","Modulo","Descripcion","EsquemaDB","FuncionSP","Parametros" )  
VALUES('MOVIMIENTOCUMPLEREQUISITOANULACION','CUENTAINVERSION','Verifica que el movimiento de cargo abono cumple con los requisitos de anulación','BSCL','BSCL.CuentaInversion.BD.Reglas::MovimientoCumpleRequisitoDeAnulacion',' p_idcargoabonomovimiento');
INSERT INTO "BSCL.Plataforma.BD::MRE.Regla" ( "CodigoRegla","Modulo","Descripcion","EsquemaDB","FuncionSP","Parametros" )  
VALUES('PRODUCTOACTIVO','TRIBUTARIO','Determinar que el Tipo de Instrumento esté activo para Interfaz','BSCL','BSCL.Tributario.BD.Reglas::ProductoActivo',' ');
INSERT INTO "BSCL.Plataforma.BD::MRE.Regla" ( "CodigoRegla","Modulo","Descripcion","EsquemaDB","FuncionSP","Parametros" )  
VALUES('MONTOPERMITIDO','TRIBUTARIO','Determinar que el monto a Informar cumpla con Rango exigido por la Norma','BSCL','BSCL.Tributario.BD.Reglas::MontoValido',' 'p_tipoinstrumento',p_montonominal');


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
