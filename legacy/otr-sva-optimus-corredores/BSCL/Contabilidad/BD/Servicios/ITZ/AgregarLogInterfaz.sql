SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.AgregarLogInterfaz";

CREATE PROCEDURE
-- 
--  Proposito: Agregar un log generado por la Interfaz Contable de auditoria/error/informativo al sistema
--  
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: RABREU
--  Fecha Creacion: 22/08/2016
--  Id Servicio = 120
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.AgregarLogInterfaz" (
      IN p_Log TABLE (  "IdTipoLog"                        BIGINT,
                        "IdEmpresa"                        BIGINT,
                        "Ejercicio"                        BIGINT,
                        "IdTipoProceso"                    BIGINT,
                        "Proceso"                          NVARCHAR(20),
                        "FechaSistemaLog"                  NVARCHAR(10),
                        "CodigoUsuario"                    NVARCHAR(40),
                        "Glosa"                            NVARCHAR(600),
                        "IdCentralizacion"                 BIGINT,
                        "IdMovimiento"                     BIGINT,
                        "IdEvento"                         BIGINT,
                        "IdDefinicionAsiento"              BIGINT,
                        "IdPreComprobante"                 BIGINT,
                        "IdPreComprobanteAsiento"          BIGINT,
                        "IdCuentaContable"                 NVARCHAR(12),
                        "IdCondicion"                      BIGINT,
                        "IdTipoContabilidad"               BIGINT,
                        "IdTipoEvento"                     BIGINT,
                        "IdTipoDatoEvento"                 NVARCHAR(30),
                        "IdTipoCentralizacion"             BIGINT,
                        "IdTipoMovimiento"                 NVARCHAR(10),
                        "NumeroMovimiento"                 BIGINT,
                        "FechaMovimiento"                  NVARCHAR(10),
                        "RutMovimiento"                    NVARCHAR(11),
                        "CuentaMovimiento"                 BIGINT,
                        "IndicadorError"                   NVARCHAR(50),
                        "Estado"                           NVARCHAR(1))
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN 
      --------------------------------------------------------- CONTROL DE ERRORES
      --- Excepcion
      DECLARE EXIT HANDLER FOR SQLEXCEPTION
      BEGIN
            RESIGNAL; 
      END;
      --------------------------------------------------------- PREPARAR DATOS    
      BEGIN
      --------------------------------------------------------- Guardar LOG 
            v_tablaTempLog = SELECT "BSCL.Contabilidad.BD.Secuencias::IdLog".NEXTVAL AS "IdLog"
									,"IdTipoLog"
									,"IdEmpresa"
									,"Ejercicio"
									,"IdTipoProceso"
									,"Proceso"
									,"FechaSistemaLog"
									,"CodigoUsuario"
									,"Glosa"
									,"IdCentralizacion"
									,"IdMovimiento"
									,"IdEvento"
									,"IdDefinicionAsiento"
									,"IdPreComprobante"
									,"IdPreComprobanteAsiento"
									,"IdCuentaContable"
									,"IdCondicion"
									,"IdTipoContabilidad"
									,"IdTipoEvento"
									,"IdTipoDatoEvento"
									,"IdTipoCentralizacion"
									,"IdTipoMovimiento"
									,"NumeroMovimiento"
									,"FechaMovimiento"
									,"RutMovimiento"
									,"CuentaMovimiento"
									,"IndicadorError"
									,"Estado"
							   FROM :p_Log; 
           
            
                  INSERT INTO "BSCL.Contabilidad.BD::ITZ.Log" (
																"IdLog"
																,"IdTipoLog"
																,"IdEmpresa"
																,"Ejercicio"
																,"IdTipoProceso"
																,"Proceso"
																,"Fecha"
																,"Hora"
																,"CodigoUsuario"
																,"Glosa"
																,"IdCentralizacion"
																,"IdMovimiento"
																,"IdEvento"
																,"IdDefinicionAsiento"
																,"IdPreComprobante"
																,"IdPreComprobanteAsiento"
																,"IdCuentaContable"
																,"IdCondicion"
																,"IdTipoContabilidad"
																,"IdTipoEvento"
																,"IdTipoDatoEvento"
																,"IdTipoCentralizacion"
																,"IdTipoMovimiento"
																,"NumeroMovimiento"
																,"FechaMovimiento"
																,"RutMovimiento"
																,"CuentaMovimiento"
																,"IndicadorError"
																,"Estado"
															 )
													  SELECT 
															 "IdLog"
															,"IdTipoLog"
															,"IdEmpresa"
															,"Ejercicio"
															,"IdTipoProceso"
															,"Proceso"
															,"FechaSistemaLog"
															,CURRENT_TIME
															,"CodigoUsuario"
															,"Glosa"
															,"IdCentralizacion"
															,"IdMovimiento"
															,"IdEvento"
															,"IdDefinicionAsiento"
															,"IdPreComprobante"
															,"IdPreComprobanteAsiento"
															,"IdCuentaContable"
															,"IdCondicion"
															,"IdTipoContabilidad"
															,"IdTipoEvento"
															,"IdTipoDatoEvento"
															,"IdTipoCentralizacion"
															,"IdTipoMovimiento"
															,"NumeroMovimiento"
															,"FechaMovimiento"
															,"RutMovimiento"
															,"CuentaMovimiento"
															,"IndicadorError"
															,"Estado"
														FROM :v_tablaTempLog; 
                 
            
      END;
END;
