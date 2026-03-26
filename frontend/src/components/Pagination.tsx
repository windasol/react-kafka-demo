import './Pagination.css';

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

/** 페이지 번호 버튼 기반 페이지네이션 */
export default function Pagination({ currentPage, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) return null;

  const maxVisible = 5;
  let start = Math.max(0, currentPage - Math.floor(maxVisible / 2));
  const end = Math.min(totalPages - 1, start + maxVisible - 1);
  start = Math.max(0, end - maxVisible + 1);

  const pages: number[] = [];
  for (let i = start; i <= end; i++) {
    pages.push(i);
  }

  return (
    <div className="pagination">
      <button
        className="pagination-btn"
        disabled={currentPage === 0}
        onClick={() => onPageChange(0)}
      >
        &laquo;
      </button>
      <button
        className="pagination-btn"
        disabled={currentPage === 0}
        onClick={() => onPageChange(currentPage - 1)}
      >
        &lsaquo;
      </button>
      {pages.map((p) => (
        <button
          key={p}
          className={`pagination-btn ${p === currentPage ? 'active' : ''}`}
          onClick={() => onPageChange(p)}
        >
          {p + 1}
        </button>
      ))}
      <button
        className="pagination-btn"
        disabled={currentPage === totalPages - 1}
        onClick={() => onPageChange(currentPage + 1)}
      >
        &rsaquo;
      </button>
      <button
        className="pagination-btn"
        disabled={currentPage === totalPages - 1}
        onClick={() => onPageChange(totalPages - 1)}
      >
        &raquo;
      </button>
    </div>
  );
}
