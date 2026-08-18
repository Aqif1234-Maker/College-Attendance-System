import { useState, useEffect } from 'react';
import { useCurrentAcademicYear } from '../../api/academicYearApi';
import { useMyClasses, useMySubjects, useMyBatches } from '../../api/assignmentApi';

export function useDependentFilter() {
  const [classId, setClassId] = useState(null);
  const [subjectId, setSubjectId] = useState(null);
  const [batchId, setBatchId] = useState(null);

  const academicYearQuery = useCurrentAcademicYear();
  const classesQuery = useMyClasses();
  const subjectsQuery = useMySubjects(classId);
  const batchesQuery = useMyBatches(classId, subjectId);

  // Selecting a Class must reset Subject and Batch — never leave a stale child selected
  // against a new parent (spec §3: "invalid combinations must never be selectable").
  function selectClass(newClassId) {
    setClassId(newClassId);
    setSubjectId(null);
    setBatchId(null);
  }

  function selectSubject(newSubjectId) {
    setSubjectId(newSubjectId);
    setBatchId(null);
  }

  function selectBatch(newBatchId) {
    setBatchId(newBatchId);
  }

  function reset() {
    setClassId(null);
    setSubjectId(null);
    setBatchId(null);
  }

  const selectedSubject = subjectsQuery.data?.find((s) => s.id === subjectId) ?? null;

  useEffect(() => {
    const batches = batchesQuery.data ?? [];

    if (!selectedSubject || batches.length === 0) {
      return;
    }

    if (selectedSubject.type === 'TH') {
      const wholeClassBatch = batches.find((batch) => batch.label === 'Entire Class') ?? batches[0];
      if (wholeClassBatch && batchId !== wholeClassBatch.id) {
        setBatchId(wholeClassBatch.id);
      }
      return;
    }

    if (batchId && !batches.some((batch) => batch.id === batchId)) {
      setBatchId(null);
    }
  }, [selectedSubject, batchesQuery.data, batchId]);

  return {
    academicYear: academicYearQuery.data,
    academicYearLoading: academicYearQuery.isLoading,

    classes: classesQuery.data ?? [],
    classesLoading: classesQuery.isLoading,
    classId,
    selectClass,

    subjects: subjectsQuery.data ?? [],
    subjectsLoading: subjectsQuery.isLoading,
    subjectId,
    selectedSubject,
    selectSubject,

    batches: batchesQuery.data ?? [],
    batchesLoading: batchesQuery.isLoading,
    batchId,
    selectBatch,

    isComplete: Boolean(classId && subjectId && batchId),
    reset
  };
}
