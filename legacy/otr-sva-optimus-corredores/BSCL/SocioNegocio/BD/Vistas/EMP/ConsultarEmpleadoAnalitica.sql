SET SCHEMA "BSCL"; 
   
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleadoAnalitica"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleadoAnalitica"  AS 
SELECT EMPLEADO."IdEmpleado"
 	                 ,CAST(EMPLEADO."CodigoEmpleado" AS NVARCHAR(40)) AS "CodigoEmpleado"
 	                 ,CASE WHEN EMPLEADO."IdPersona" = 0 THEN NULL ELSE EMPLEADO."IdPersona" END AS "IdPersona"  
 	                 ,CASE WHEN EMPLEADO."IdEmpresa" = 0 THEN NULL ELSE EMPLEADO."IdEmpresa" END AS "IdEmpresa"  
 	                 ,CASE WHEN EMPLEADO."IdSucursal" = 0 THEN NULL ELSE EMPLEADO."IdSucursal" END AS "IdSucursal" 
 	                 ,CASE WHEN IFNULL(EMPLEADO."FechaCreacion",'')= '' THEN '0001-01-01 00:00:00' 
 	                              ELSE  
 	                                  CASE WHEN  EMPLEADO."FechaCreacion" = '0001-01-01 00:00:00' THEN '0001-01-01 00:00:00' 
 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (EMPLEADO."FechaCreacion", CONFIGURACION."Valor")),19)
 	                                  END 
 	                  END AS "FechaCreacion"
             		,CASE WHEN IFNULL(EMPLEADO."FechaModificacion",'')= '' THEN '0001-01-01 00:00:00' 
 	                              ELSE  
 	                                  CASE WHEN  EMPLEADO."FechaModificacion" = '0001-01-01 00:00:00' THEN '0001-01-01 00:00:00' 
 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (EMPLEADO."FechaModificacion", CONFIGURACION."Valor")),19)
 	                                  END 
 	                  END AS "FechaModificacion"
 	                 ,CASE WHEN IFNULL(EMPLEADO."FechaIngreso",'')= '' THEN '0001-01-01 00:00:00' 
 	                              ELSE  
 	                                  CASE WHEN  EMPLEADO."FechaIngreso" = '0001-01-01 00:00:00' THEN '0001-01-01 00:00:00' 
 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (EMPLEADO."FechaIngreso", CONFIGURACION."Valor")),19)
 	                                  END 
 	                  END AS "FechaIngreso"
 	                 ,CASE WHEN EMPLEADO."IdEstado" = '' THEN NULL ELSE EMPLEADO."IdEstado" END AS "IdEstado" 
 	                 ,CAST(COALESCE(ESTADOEMPLEADO."Nombre",'') AS NVARCHAR(100)) AS "NombreEstadoEmpleado"             
			 ,TO_CHAR(EMPLEADO."FechaInicioCargo") AS "FechaInicioCargo"
	  		 ,case when IFNULL(TO_CHAR(EMPLEADO."FechaTerminoCargo"),'0001-01-01') = '' then '0001-01-01' else IFNULL(TO_CHAR(EMPLEADO."FechaTerminoCargo"),'0001-01-01') end AS "FechaTerminoCargo"
			 ,CASE WHEN EMPLEADO."CodigoTipoCargoEmpleado" = '' THEN NULL ELSE EMPLEADO."CodigoTipoCargoEmpleado" END AS "CodigoTipoCargoEmpleado" 
			 ,CAST(COALESCE(LEFT(TIPOCARGOEMPLEADO."NombreTipoCargoEmpleado",100),'') AS NVARCHAR(100))  AS "NombreTipoCargoEmpleado"
			FROM "BSCL.SocioNegocio.BD::EMP.Empleado"  AS EMPLEADO
			CROSS JOIN (select top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion"
			            where "CodigoParametro" = 'PLA.SegundosZonaHoraria') AS CONFIGURACION
			LEFT JOIN "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"  AS TIPOCARGOEMPLEADO
			 ON EMPLEADO."CodigoTipoCargoEmpleado" =  TIPOCARGOEMPLEADO."CodigoTipoCargoEmpleado"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEstadoEmpleado" AS ESTADOEMPLEADO
			 ON  ESTADOEMPLEADO."IdEstado" = EMPLEADO."IdEstado"; 

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleadoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 