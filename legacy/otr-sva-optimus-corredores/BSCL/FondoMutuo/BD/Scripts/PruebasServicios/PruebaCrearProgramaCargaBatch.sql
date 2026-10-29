set schema bscl;

create local temporary table #cargas ("IdCarga" BIGINT);
insert into #cargas values(68);

call "BSCL.FondoMutuo.BD.Servicios::CrearProgramaCargaBatch" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_IdCargas => #cargas,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);
DROP TABLE #cargas;

/*
SET SCHEMA "BSCL";
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
insert into #param values('Transacciones','60');
insert into #param values('IdEmpresa','1');
insert into #param values('IdUsuario','21');
						   
CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"(
1,
3,
'FMM',
0,
'CARGAMASIVASOLICITUDESFFMM',
5,
#param,
'S',
?,
?,
?,
?,
?);
DROP TABLE  #param;
select * from "BSCL"."BSCL.Plataforma.BD::BAT.Programa" ORDER BY "IdPrograma" DESC;
*/

--select * from "BSCL"."BSCL.Plataforma.BD::BAT.Programa" ORDER BY "IdPrograma" DESC;

select * from "BSCL"."BSCL.Plataforma.BD::BAT.Programa" ORDER BY "IdPrograma" DESC;