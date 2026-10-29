SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarSupervisorAnalitica";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarSupervisorAnalitica" AS
SELECT  
 	 SUP."IdEmpleadoSupervisor"
 	 ,CASE WHEN IFNULL(SUP."FechaCreacion" ,'')= '' THEN '0001-01-01 00:00:00' 
 	            ELSE  
 	            CASE WHEN  SUP."FechaCreacion"  IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
 	           	ELSE LEFT(TO_CHAR(ADD_SECONDS (SUP."FechaCreacion" , CONFIGURACION."Valor")),19)
 	            END 
 	 		END AS "FechaCreacion"
 	 ,CASE WHEN EMP."IdEstado" = '' THEN NULL ELSE EMP."IdEstado" END AS "IdEstado" 
     ,CAST(EMP."CodigoEmpleado" AS NVARCHAR(40)) AS "CodigoEmpleado"
     ,EMP."IdEmpresa"
     ,CASE WHEN PER."IdPersona" = 0 THEN NULL ELSE PER."IdPersona" END AS "IdPersona" 
     ,CAST(PER."NombreCompleto" AS NVARCHAR(302)) AS "NombreCompleto"
     from  "BSCL.SocioNegocio.BD::EJE.Supervisor" as SUP
     inner join "BSCL.SocioNegocio.BD::EMP.Empleado" as EMP
		on EMP."IdEmpleado" = SUP."IdEmpleadoSupervisor"
	 inner join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
	    on PER."IdPersona" = EMP."IdPersona"
	 CROSS JOIN (select top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion"
	            where "CodigoParametro" = 'PLA.SegundosZonaHoraria') AS CONFIGURACION;

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarSupervisorAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;