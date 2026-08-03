import { useCoordinatorClasses } from '../../hooks/useCoordinatorClasses';
import Card from '../../shared/ui/Card';
import Table from '../../shared/ui/Table';

export default function AssignedClassesPage() {
  const { currentYearQuery, classes, isLoading } = useCoordinatorClasses();

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-semibold text-gray-900">Assigned Classes</h1>
        <p className="text-sm text-gray-500 mt-1">
          Current year: {currentYearQuery.data?.label ?? 'None set'}
        </p>
      </div>

      <Card>
        {isLoading ? (
          <div className="text-sm text-gray-400 py-4 text-center">Loading…</div>
        ) : (
          <Table
            columns={[
              { key: 'name', header: 'Class' },
              { key: 'academicYearLabel', header: 'Academic Year' }
            ]}
            rows={classes.map((c) => ({ ...c, id: c.id }))}
            emptyMessage="You are not assigned as Coordinator of any class in the current academic year."
          />
        )}
      </Card>
    </div>
  );
}
