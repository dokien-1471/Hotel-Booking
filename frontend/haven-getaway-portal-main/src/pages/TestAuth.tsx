import React, { useState } from 'react';
import AuthModal from '@/components/AuthModal';
import { Button } from '@/components/ui/button';

const TestAuth = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState<'login' | 'register'>('login');

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="text-center">
        <h1 className="text-2xl font-bold mb-4">Test Auth Modal</h1>

        <div className="space-x-4">
          <Button
            onClick={() => {
              setModalMode('login');
              setIsModalOpen(true);
            }}
            className="bg-blue-600 hover:bg-blue-700"
          >
            Test Login
          </Button>

          <Button
            onClick={() => {
              setModalMode('register');
              setIsModalOpen(true);
            }}
            className="bg-green-600 hover:bg-green-700"
          >
            Test Register
          </Button>
        </div>

        <AuthModal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          mode={modalMode}
          onModeChange={setModalMode}
        />

        {/* Hiển thị thông tin đăng nhập nếu có */}
        <div className="mt-8 p-4 bg-white rounded-lg shadow">
          <h2 className="text-lg font-semibold mb-2">Current Auth State:</h2>
          <pre className="text-left bg-gray-50 p-4 rounded">
            {JSON.stringify({
              token: localStorage.getItem('token'),
              user: localStorage.getItem('user')
            }, null, 2)}
          </pre>
        </div>
      </div>
    </div>
  );
};

export default TestAuth; 