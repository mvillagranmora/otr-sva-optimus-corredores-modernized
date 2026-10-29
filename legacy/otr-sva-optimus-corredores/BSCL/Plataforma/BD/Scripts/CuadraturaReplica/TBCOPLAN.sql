select * from "2022_05_31_TBCOPLAN" t where EJERCICIO  = 2022;

select * from "BSCL.Replica.BD.Vistas::Sebra.TBCOPLAN" brbvst where EJERCICIO  = 2022 ;

--cuadre de cuentas contables

select * 
from "2022_05_31_TBCOPLAN" t
left join "BSCL.Replica.BD.Vistas::Sebra.TBCOPLAN" t2 on t2.CTA_CONTAB  = t.CTA_CONTAB 
where t.EJERCICIO  = 2022 and (t2.CTA_CONTAB is null or t.CTA_CONTAB is null)

--faltan 24 cuentas en optimus

select * from "BSCL.Replica.BD.Vistas::Sebra.TBCOPLAN" brbvst where EJERCICIO  = 2022 ;


solo se puede cuadrar
MONTO_DEBE_PER_003
MONTO_HABER_PER_003