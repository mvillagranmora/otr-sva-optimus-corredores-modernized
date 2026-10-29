--limpieza 12 meses
delete FROM "BSCL.Plataforma.BD::EVT.EventoArchivoAdjunto" where "IdEvento"
in (select "IdEvento" FROM "BSCL.Plataforma.BD::EVT.Evento" where "FechaCreacion"  <= ADD_DAYS(CURRENT_DATE,-30*12));

delete FROM "BSCL.Plataforma.BD::EVT.EventoDirecciones" where "IdEvento"
in (select "IdEvento" FROM "BSCL.Plataforma.BD::EVT.Evento" where "FechaCreacion"  <= ADD_DAYS(CURRENT_DATE,-30*12));

delete FROM "BSCL.Plataforma.BD::EVT.EventoDetalle" where "IdEvento"
in (select "IdEvento" FROM "BSCL.Plataforma.BD::EVT.Evento" where "FechaCreacion"  <= ADD_DAYS(CURRENT_DATE,-30*12));

delete FROM "BSCL.Plataforma.BD::EVT.Evento" where "FechaCreacion"  <= ADD_DAYS(CURRENT_DATE,-30*12);

delete FROM "BSCL.Plataforma.BD::EVT.OrigenEvento" where "IdOrigenEvento" IN (
SELECT O."IdOrigenEvento" from "BSCL.Plataforma.BD::EVT.OrigenEvento" O
LEFT JOIN "BSCL.Plataforma.BD::EVT.Evento" e on o."IdOrigenEvento" = e."IdOrigenEvento" 
group by O."IdOrigenEvento" HAVING COUNT(e."IdEvento") = 0
);

delete from BSCL."BSCL.Plataforma.BD::EVT.NotificacionDetalle" where "IdNotificacion" in (
	select "IdNotificacion" from BSCL."BSCL.Plataforma.BD::EVT.Notificacion" where "FechaCreacion" <= ADD_DAYS(CURRENT_DATE,-30*12)
)

delete from BSCL."BSCL.Plataforma.BD::EVT.Notificacion" where "FechaCreacion" <= ADD_DAYS(CURRENT_DATE,-30*12);

delete from "BSCL.Plataforma.BD::EVT.Destinatario" where "IdEmail" in (
	select "IdEmail" from "BSCL.Plataforma.BD::EVT.Mail" where "FechaCreacion" <= ADD_DAYS(CURRENT_DATE,-30*12)
);

delete from "BSCL.Plataforma.BD::EVT.Mail" where "FechaCreacion" <= ADD_DAYS(CURRENT_DATE,-30*12);

delete from BSCL."BSCL.Contabilidad.BD::ITZ.MovimientoDato"
where "IdMovimiento" IN (
	SELECT "IdMovimiento" FROM BSCL."BSCL.Contabilidad.BD::ITZ.Movimiento" WHERE "Fecha" <= ADD_DAYS(CURRENT_DATE,-30*12) 
);

delete from BSCL."BSCL.Contabilidad.BD::ITZ.Movimiento" WHERE "Fecha" <= ADD_DAYS(CURRENT_DATE,-30*12);

delete from BSCL."BSCL.Contabilidad.BD::ITZ.Log" WHERE "Fecha" <= ADD_DAYS(CURRENT_DATE,-30*12);

delete from BSCL."BSCL.Contabilidad.BD::ITZ.Evento" WHERE "FechaProceso" <= ADD_DAYS(CURRENT_DATE,-30*12);


--limpieza 3 meses
DELETE FROM bscl."BSCL.Plataforma.BD::PLA.AuditoriaWebServices" 
WHERE "FechaInicio" <= ADD_DAYS(CURRENT_DATE,-90);

DELETE FROM bscl."BSCL.Plataforma.BD::PLA.AuditoriaServicios" 
WHERE "FechaRegistro" <= ADD_DAYS(CURRENT_DATE,-90) AND "IdServicio" NOT IN (1722,20024,20025,13);

delete from bscl."BSCL.CuentaInversion.BD::CTA.PatrimonioCaja" where "FechaPatrimonio" <= ADD_DAYS(CURRENT_DATE,-90);

delete from bscl."BSCL.Custodia.BD::CUS.PatrimonioCustodia" where "FechaPatrimonio" <= ADD_DAYS(CURRENT_DATE,-90);

delete from BSCL."BSCL.Normativa.BD::CIR.ArchivoNCG365" where "FechaCertificacion" <= ADD_DAYS(CURRENT_DATE,-90);

delete from BSCL."BSCL.Plataforma.BD::PLA.ResumenReplica" where "FechaReal" <= ADD_DAYS(CURRENT_DATE,-90);

--limpieza 30 dias
DELETE FROM bscl."BSCL.Orden.BD::ORD.ControlMercado" WHERE "FechaIngresoRegistro" <= ADD_DAYS(CURRENT_DATE,-30);


/*update BSCL."BSCL.Plataforma.BD::INS.Instrumento" x
set "RazonSocial" = replace("RazonSocial",'%¤','N')
where "RazonSocial" like '%%¤%';

update BSCL."BSCL.Plataforma.BD::INS.Instrumento" x
set "RazonSocial" = replace("RazonSocial",'O¤','''')
where "RazonSocial" like '%O¤%';*/

select "FechaRespaldo" ,count(1) 
from "BSCL.Custodia.BD::HIS.Cartera" bcbhc where "FechaRespaldo" <= ADD_DAYS(CURRENT_DATE,-30*24)
group by "FechaRespaldo" 


select * from "BSCL.Custodia.BD::HIS.Cartera" bcbhc where "FechaRespaldo" < '2022-03-31';
select * from "BSCL.Custodia.BD::HIS.Cartera" bcbhc where "FechaRespaldo" >= '2022-03-31' and "FechaRespaldo" < '2022-06-30';
select * from "BSCL.Custodia.BD::HIS.Cartera" bcbhc where "FechaRespaldo" >= '2022-06-30' and "FechaRespaldo" < '2022-09-30';
select * from "BSCL.Custodia.BD::HIS.Cartera" bcbhc where "FechaRespaldo" >= '2022-09-30' and "FechaRespaldo" < '2022-12-31';



--limpieza his cartera
SELECT 'DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"=''' || to_nvarchar("FechaRespaldo",'YYYY-MM-DD') || ''';' as del,
to_nvarchar("FechaRespaldo",'YYYY-MM-DD') as fecha,count(1) 
FROM bscl."BSCL.Custodia.BD::HIS.Cartera"
WHERE "FechaRespaldo" <= ADD_DAYS(CURRENT_DATE,-18*30)
GROUP BY "FechaRespaldo"
ORDER BY "FechaRespaldo" DESC

DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-12-29';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-12-28';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-12-27';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-12-26';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-12-22';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-12-21';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-12-20';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-12-19';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-12-18';
COMMIT;
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-12-15';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-12-14';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-12-13';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-12-12';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-12-11';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-12-07';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-12-06';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-12-05';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-12-04';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-12-01';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-29';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-28';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-27';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-24';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-23';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-22';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-21';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-20';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-17';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-16';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-15';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-14';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-13';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-10';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-09';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-08';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-07';
COMMIT;
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-06';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-03';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-11-02';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-10-30';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-10-26';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-10-25';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-10-24';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-10-23';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-10-20';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-10-19';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-10-18';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-10-17';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-10-16';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-10-13';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-10-12';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-10-11';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-10-10';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-10-06';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-10-05';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-10-04';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-10-03';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-10-02';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-09-29';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-09-28';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-09-27';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-09-26';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-09-25';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-09-22';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-09-21';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-09-20';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-09-15';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-09-14';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-09-13';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-09-12';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-09-11';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-09-08';
COMMIT;
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-09-07';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-09-06';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-09-05';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-09-04';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-09-01';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-30';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-29';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-28';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-25';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-24';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-23';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-22';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-21';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-18';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-17';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-16';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-14';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-11';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-10';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-09';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-08';
COMMIT;
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-07';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-04';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-03';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-02';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-08-01';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-28';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-27';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-26';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-25';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-24';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-21';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-20';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-19';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-18';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-17';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-14';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-13';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-12';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-11';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-10';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-07';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-06';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-05';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-04';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-07-03';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-06-29';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-06-28';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-06-27';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-06-23';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-06-22';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-06-20';
COMMIT;
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-06-19';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-06-16';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-06-15';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-06-14';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-06-13';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-06-12';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-06-09';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-06-08';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-06-07';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-06-06';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-06-05';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-06-02';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-06-01';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-30';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-29';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-26';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-25';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-24';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-23';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-22';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-19';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-18';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-17';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-16';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-15';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-12';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-11';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-10';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-09';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-08';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-05';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-04';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-03';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-05-02';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-04-28';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-04-27';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-04-26';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-04-25';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-04-24';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-04-21';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-04-20';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-04-19';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-04-18';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-04-17';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-04-14';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-04-13';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-04-12';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-04-11';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-04-10';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-04-06';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-04-05';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-04-04';
DELETE FROM BSCL."BSCL.Custodia.BD::HIS.Cartera" where "FechaRespaldo"='2023-04-03';
COMMIT;

