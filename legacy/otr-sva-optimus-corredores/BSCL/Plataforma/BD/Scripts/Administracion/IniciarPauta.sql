set schema bscl;
drop table #asdf;
create local temporary table #asdf ("NombreParametro" nvarchar(100) , "Valor" nvarchar(5000));
insert into #asdf
select 'FechaSistemaPauta','2025-01-10' from dummy;



insert into #asdf
select 'IdEmpresa','1' from dummy;


insert into #asdf
select 'IdUsuario','6792' from dummy;

call "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"
 (
	1,--IN p_IdEmpresa          BIGINT,
	0,--IN p_IdUsuario          BIGINT,
	'PLA',--IN p_IdModulo           NVARCHAR(3),
	0,--IN	p_IdAgenda	 		BIGINT, 
	'PAUTA_BASE',--IN	p_IdTipoTarea		NVARCHAR(40),   
	0,--IN	p_Segundos			BIGINT,
	#asdf,--IN  p_TablaParametros table("NombreParametro" nvarchar(100) , "Valor" nvarchar(5000)) DEFAULT EMPTY,
	'S',--IN  p_CreaParam			NVARCHAR(1),
	?,--OUT p_out_IdPrograma		table("IdPrograma" BIGINT),
	?,--OUT p_out_AdvertenciasContador int,
	?,--OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	?,--OUT p_out_ErroresContador int,
	?--OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	-- Salidas para mensajes de advertencias y errores									
);

commit;

rollback

select * from "BSCL.Plataforma.BD::PAU.EjecucionDetalle" bpbped order by "IdEjecucion" desc





select  "Nemotecnico" ,count(1) from "BSCL.Custodia.BD::CUS.Cartera" bcbhc 
where "IdClasificacionTipoInstrumento" = 'RV' and "IdTipoInstrumento" = 'CFI' AND "IdEstado" = 'VIG'
and "Nemotecnico" like 'CFIFYNDE%'
group by "Nemotecnico" 
order by "Nemotecnico" 


select *  
from "BSCL.Custodia.BD::CUS.Cartera" bcbhc 
where "IdClasificacionTipoInstrumento" = 'RV' and "IdTipoInstrumento" = 'CFI' AND "IdEstado" = 'VIG'
and "Nemotecnico" = 'CFIFYNDE3A'
