       
-----------------------------------------------------------------------------------------------------------------------------------------
------------- CARGAR PERSONAS ----------------
-----------------------------------------------------------------------------------------------------------------------------------------
--EJECUTAR
CALL "BSCL"."BSCL.Homologacion.BD.Servicios::CargarDatosPersona"(22,'2015-12-02',1,?,?,?);

--VER LOG
select * from "BSCL"."BSCL.Plataforma.BD::PLA.Log" where "IdEntidad" = 'PROCESOCARGA'; --"IdModulo" = 'CLI';

-- REVISAR
SELECT count(*) FROM "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" WHERE "IdCarga" = 22;
SELECT count(*) FROM "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" WHERE "IdCarga" = 22;
SELECT count(*) FROM "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisica" WHERE "IdCarga" = 22;
SELECT count(*) FROM "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionElectronica" WHERE "IdCarga" = 22;
SELECT count(*) FROM "BSCL"."BSCL.SocioNegocio.BD::PER.Telefono" WHERE "IdCarga" = 22;

SELECT * FROM "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 	WHERE "IdCarga" = 22 ;
SELECT * FROM "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisica" 		WHERE "IdCarga" = 22 ;
SELECT * FROM "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionElectronica" 	WHERE "IdCarga" = 22 ;
SELECT * FROM "BSCL"."BSCL.SocioNegocio.BD::PER.Telefono" 				WHERE "IdCarga" = 22 ;
select * FROM "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" WHERE "IdCarga" = 22;
select count(*) FROM "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as SOC
,"BSCL"."BSCL.SocioNegocio.BD::PER.Persona" PER
where SOC."IdPersona" = PER."IdPersona"
and PER."IdCarga" = 22;

select count(*) FROM "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLI
,"BSCL"."BSCL.SocioNegocio.BD::PER.Persona" PER
where CLI."IdPersona" = PER."IdPersona"
and PER."IdCarga" = 22;


-- LIMPIAR CARGA
delete from 
"BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"
where exists ( select 1 from "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PER
                 where PER."IdPersona" = "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"."IdPersona"
                  and PER."IdCarga" = 22);
delete FROM "BSCL"."BSCL.SocioNegocio.BD::PER.Telefono" WHERE "IdCarga" = 22;
delete FROM "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionElectronica" WHERE "IdCarga" = 22;
delete FROM "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisica" WHERE "IdCarga" = 22;
delete FROM "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" WHERE "IdCarga" = 22;
delete FROM "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" WHERE "IdCarga" = 22;


--- DOCUMENTO
select "DocumentoIdentidad", count(*) from "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 
--where "IdCarga" = 99
group by "DocumentoIdentidad"
having count(*) > 1;

----------------------------------------------------------
------------- CARGAR CLIENTES/PORTAFOLIOS ----------------
----------------------------------------------------------
CALL "BSCL"."BSCL.Homologacion.BD.Servicios::CargarDatosClientesPortafolios"(22,'2015-12-02',1,?,?,?);

--- CONSULTAR CARGA
select * from "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" where "IdCarga" = 22 ;
select * from "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" where "IdCarga" = 22 ;

--- ELIMINAR REGISTRO COMPLETO DE CLIENTE
select * from "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" where "IdCarga" = 99 and "IdCliente" = 109911;--109613;--109613;
select * from "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" where "IdCarga" = 99 and "IdCliente" = 109911;-- 109613;

--- para eliminacion FULL
/*
DELETE FROM  "BSCL.Portafolio.BD::POR.Portafolio"; 
DELETE FROM  "BSCL.SocioNegocio.BD::CLI.Cliente"; 
ALTER SEQUENCE "BSCL.Portafolio.BD.Secuencias::Portafolio" RESTART WITH 10000;
ALTER SEQUENCE "BSCL.SocioNegocio.BD.Secuencias::Cliente" RESTART WITH 10000;
*/


SELECT * FROM "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 	WHERE "IdCarga" = 99 and "IdPersona" = 163107;--162809;
SELECT * FROM "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisica" 		WHERE "IdCarga" = 99 and "IdPersona" = 163107;
SELECT * FROM "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionElectronica" 	WHERE "IdCarga" = 99 and "IdPersona" = 163107;
SELECT * FROM "BSCL"."BSCL.SocioNegocio.BD::PER.Telefono" 				WHERE "IdCarga" = 99 and "IdPersona" = 163107;
select * FROM "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 		WHERE "IdPersona" = 163107;
select * FROM "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" WHERE "IdCarga" = 99 and "IdPersona" = 163107;



DELETE from "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" where "IdCarga" = 99 and "IdCliente" = 109911;--109613;--109613;
DELETE from "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" where "IdCarga" = 99 and "IdCliente" = 109911;-- 109613;

DELETE FROM "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 	WHERE "IdCarga" = 99 and "IdPersona" = 163107;--162809;
DELETE FROM "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisica" 		WHERE "IdCarga" = 99 and "IdPersona" = 163107;
DELETE FROM "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionElectronica" 	WHERE "IdCarga" = 99 and "IdPersona" = 163107;
DELETE FROM "BSCL"."BSCL.SocioNegocio.BD::PER.Telefono" 				WHERE "IdCarga" = 99 and "IdPersona" = 163107;
DELETE FROM "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 		WHERE "IdPersona" = 163107;
DELETE FROM "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" WHERE "IdCarga" = 99 and "IdPersona" = 163107;


---- CHEQUEO
-- 1) Personas que no son Socios de Negocio

		select count(*)
		from  "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as Persona
		where not exists(select 1 from "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC where Persona."IdPersona"  = SOC."IdPersona") 
		-- NO HAY CASOS

-- 2) Personas que son clientes, marcar en Socio de Negocio el Codigo de Cliente

-- 35 Clientes si estan marcados en 
SELECT count(*) FROM "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" PER, 
							"BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" CLI
where PER."IdPersona" = CLI."IdPersona"
and not exists (select 1 from "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC where PER."IdPersona"  = SOC."IdPersona" and SOC."IdCliente" = CLI."IdCliente") 

UPDATE 	"BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"
SET 	"IdCliente" = ( select max(CLI."IdCliente")  --  grabar ID Cliente en Socio de NEgocio
						from "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" PER, "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" CLI
						where PER."IdPersona" = CLI."IdPersona" --- persona tenga un cliente creado
						and   PER."IdPersona" = "BSCL"."BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"."IdPersona" -- persona creada como socio de negocio
					  );


----------------------------------------------------------
------------- CARGAR CUSTODIAS ---------------------------
----------------------------------------------------------

set schema bscl;

CALL "BSCL.Homologacion.BD.Servicios::CargarCustodia"
	(  
	 	1--IN  p_IdEmpresa		   			
     	,1--IN  p_IdUsuario		   			
	 	,88--IN  p_IdCarga 	       			
	 	,'CUS'--IN  p_Modulo           			
     	,1--IN  P_Modalidad        			
     	,' 77968070-3'--IN  p_RutCliente       			
	 	,'2015-08-01'--IN  p_FechaInicial            
		,'2016-01-22'---IN  p_FechaFinal				
        ,?--OUT p_out_ErroresContador int,
		,?--OUT p_out_ErroresDetalle 
    );
    
    
CALL "BSCL.Homologacion.BD.Servicios::CargarCustodiaUltimoMesEnCurso"    
	(  
	 	1--IN  p_IdEmpresa		   			
     	,1--IN  p_IdUsuario		   			
	 	,88--IN  p_IdCarga 	       			
	 	,'CUS'--IN  p_Modulo           			
     	,1--IN  P_Modalidad        			
     	,' 77968070-3'--IN  p_RutCliente       			
	 	,'2016-02-01'--IN  p_FechaInicial            
		,'2016-02-22'---IN  p_FechaFinal				
        ,?--OUT p_out_ErroresContador int,
		,?--OUT p_out_ErroresDetalle 
    );    
     

select * from "BSCL_VIRTUAL"."GES_TBPLPASI"

select "Fecha","FechaLiquidacion","Nemo","INGCantidad","IdEstado","INGFecha","EGRFecha","IdCliente","IdPortafolio","IdSecuenciaCarteraMigracion","CodigoPeriodo",* 
from "BSCL.Custodia.BD::CUS.Cartera" 
where "IdCarga" = 88 and "Nemo" = 'CHILE' and "IdCliente" = 248819

order by "INGFecha";

select "Nemo","INGCantidad","IdEstado","INGFecha","EGRFecha","IdCliente","IdPortafolio","IdCarga","IdInstrumento" 
from "BSCL.Custodia.BD::GTA.Garantia" 
where "IdCliente" = 248819 --"IdCarga" = 88 
order by "INGFecha";


delete from "BSCL.Custodia.BD::GTA.Garantia" where "IdCarga" = 88;
delete from "BSCL.Custodia.BD::CUS.Cartera" where "IdCarga" = 88;


----------------- REVISAR DATOS SEBRA ---------------------
select * from "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" where "DocumentoIdentidad" = '661194-K'

select * FROM
"BSCL_VIRTUAL"."GES_TBCTCIRV"
WHERE "COD_PER" = '2015-10' 
and ltrim(rtrim("RUT_CLI")) = '661194-K'


select sum("CAN_ACTUAL") FROM
"BSCL_VIRTUAL"."GES_TBCTCIRV"
WHERE "COD_PER" = '2015-10' 
and ltrim(rtrim("RUT_CLI")) = '661194-K'


----------------- EJECUTAR CARGA        -----------------------------	 	
call "BSCL"."BSCL.Homologacion.BD.Servicios::CargarCustodiaRV"	(  1,1, 222,'CUS', 'I', '   661194-K','2015-10-01' ,'2015-10-30',?,?)
	 	
----------------- REVISAR DATOS OPTIMUS -----------------------------	 	




