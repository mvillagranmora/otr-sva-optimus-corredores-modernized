SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::CalculaPreferenciaCalceLibroInventario";

CREATE PROCEDURE
-- 
--  Proposito: 
--   
 
"BSCL.Plataforma.BD.Servicios::CalculaPreferenciaCalceLibroInventario" ( 
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          BIGINT,
    IN p_IdUsuario          BIGINT,
    IN p_IdModulo           NVARCHAR(3),

	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
AS	
BEGIN
	p_out_AdvertenciasContador	:= 1;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	

	BEGIN
		
		DECLARE MULTIPLICADOR INT:=1000000;
		DECLARE SEC_NETEO_DEFAULT INT;
		
		select ifnull(max("IdTipoCriterioNeteo"),0)+1 into SEC_NETEO_DEFAULT from "BSCL.Plataforma.BD::PLA.TipoCriterioNeteo";
		
		delete from "BSCL.CuentaInversion.BD::CTA.PreferenciaCalceLibroInventario";

		insert into "BSCL.CuentaInversion.BD::CTA.PreferenciaCalceLibroInventario"
		("IdPreferenciaCalceLibroInventario","IdConcepto","EsPagadoCon","Preferencia","IdEstado")			
		select 
			row_number() over (order by "Preferencia") as "IdPreferenciaLibroCalceInventario",
			t."IdConcepto",t."EsPagadoCon",t."Preferencia",'ACT' as "IdEstado"
		from (
			select 
				conceptoA."IdConcepto" as "IdConcepto",conceptoB."IdConcepto" as "EsPagadoCon",
				:MULTIPLICADOR * ifnull(tcn."SecuenciaNeteo",:SEC_NETEO_DEFAULT) + (ROW_NUMBER() OVER (PARTITION BY ifnull(tcn."SecuenciaNeteo",:SEC_NETEO_DEFAULT) ORDER BY ifnull(tcn."SecuenciaNeteo",SEC_NETEO_DEFAULT),conceptoA."IdConcepto",conceptoB."IdConcepto")) as "Preferencia"
			from "BSCL.Plataforma.BD::PLA.Concepto" conceptoA
			join "BSCL.Plataforma.BD::PLA.Concepto" conceptoB 
				--on (conceptoA."IdEstado"= 'VIG' and ifnull(conceptoA."IdTipoCriterioNeteo",-9999) = ifnull(conceptoB."IdTipoCriterioNeteo",-9999))
				ON (1=1) 
			left join "BSCL.Plataforma.BD::PLA.TipoCriterioNeteo" tcn 
				on (conceptoA."IdTipoCriterioNeteo" = tcn."IdTipoCriterioNeteo")
				--where conceptoA."IdConcepto" != conceptoB."IdConcepto"
				--where conceptoA."IdEstado"= 'VIG' and conceptoB."IdEstado"= 'VIG'
			) t
		order by t."Preferencia";


		p_out_ErroresContador := 0;

	END;
	

END;
