select top 10 *
from _sys_statistics.HOST_UNCOMMITTED_WRITE_TRANSACTION


select *
from m_connections 
where user_name != 'SYSTEM' and connection_type = 'Remote'
--and connection_id = 352667
order by connection_id

select *
from M_BLOCKED_TRANSACTIONS

select * 
from m_transactions

select * 
from M_SESSION_CONTEXT 

select * 
from M_TABLE_LOCKS

select *
from M_OBJECT_LOCKS

select *
from M_OBJECT_LOCK_STATISTICS_RESET


select *
from M_RECORD_LOCKS

select * 
from M_OBJECT_LOCK_STATISTICS

select *
from M_EXPENSIVE_STATEMENTS
where schema_name = 'BSCL'
and Lock_wait_duration > 0

select *
from M_LOCK_WAITS_STATISTICS



DELETE FROM _SYS_REPO.OBJECT_HISTORY
WHERE (package_id, object_name, object_suffix, version_id)
NOT IN
(SELECT package_id, object_name, object_suffix, MAX(version_id) AS maxvid from _SYS_REPO.OBJECT_HISTORY GROUP BY package_id, object_name, object_suffix ORDER BY package_id, object_name, object_suffix );


DELETE from "_SYS_BI"."M_TIME_DIMENSION"
