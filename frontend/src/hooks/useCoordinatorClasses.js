import { useCurrentAcademicYear } from '../api/academicYearApi';
import { useMyCoordinatedClasses } from '../api/classApi';

export function useCoordinatorClasses() {
  const currentYearQuery = useCurrentAcademicYear();
  const classesQuery = useMyCoordinatedClasses(
    currentYearQuery.data?.id,
    Boolean(currentYearQuery.data?.id)
  );

  return {
    currentYearQuery,
    classesQuery,
    classes: classesQuery.data ?? [],
    isLoading: currentYearQuery.isLoading || classesQuery.isLoading
  };
}
