import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axiosClient from '../../api/axiosClient';
import Card from '../../shared/ui/Card';
import Table from '../../shared/ui/Table';
import Button from '../../shared/ui/Button';

async function fetchAllAcademicYears() {
  const response = await axiosClient.get('/academic-years');
  return response.data.data;
}

async function createAcademicYear(label) {
  const response = await axiosClient.post('/academic-years', { label });
  return response.data.data;
}

async function setCurrentAcademicYear(id) {
  const response = await axiosClient.patch(`/academic-years/${id}/set-current`);
  return response.data.data;
}

export default function AcademicYearPage() {
  const queryClient = useQueryClient();
  const [label, setLabel] = useState('');
  const [error, setError] = useState('');

  const yearsQuery = useQuery({ queryKey: ['academicYears', 'all'], queryFn: fetchAllAcademicYears });

  const createMutation = useMutation({
    mutationFn: createAcademicYear,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['academicYears'] });
      queryClient.invalidateQueries({ queryKey: ['academicYear', 'current'] });
      setLabel('');
    }
  });

  const setCurrentMutation = useMutation({
    mutationFn: setCurrentAcademicYear,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['academicYears'] });
      queryClient.invalidateQueries({ queryKey: ['academicYear', 'current'] });
    }
  });

  async function handleCreate(e) {
    e.preventDefault();
    setError('');
    try {
      await createMutation.mutateAsync(label);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not create academic year.');
    }
  }

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-semibold text-gray-900">Academic Year</h1>
        <p className="text-sm text-gray-500 mt-1">Manage academic years. Exactly one is marked current at a time.</p>
      </div>

      <Card title="Add Academic Year">
        <form onSubmit={handleCreate} className="flex gap-3 items-end">
          <div className="flex-1 max-w-xs">
            <label className="block text-xs font-medium text-gray-500 mb-1">Label</label>
            <input
              type="text"
              value={label}
              onChange={(e) => setLabel(e.target.value)}
              placeholder="e.g. 2025-2026"
              required
              className="w-full rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
          <Button type="submit" loading={createMutation.isPending}>Add</Button>
        </form>
        {error && <div className="text-sm text-red-600 mt-3">{error}</div>}
      </Card>

      <Card title="All Academic Years">
        <Table
          columns={[
            { key: 'label', header: 'Label' },
            {
              key: 'current',
              header: 'Status',
              render: (row) =>
                row.current ? (
                  <span className="text-xs font-medium text-primary-700 bg-primary-50 px-2.5 py-1 rounded-full">
                    Current
                  </span>
                ) : (
                  <button
                    onClick={() => setCurrentMutation.mutate(row.id)}
                    disabled={setCurrentMutation.isPending}
                    className="text-xs font-medium text-gray-600 hover:text-primary-600"
                  >
                    Set as Current
                  </button>
                )
            }
          ]}
          rows={(yearsQuery.data ?? []).map((y) => ({ ...y, id: y.id }))}
          emptyMessage="No academic years yet."
        />
      </Card>
    </div>
  );
}